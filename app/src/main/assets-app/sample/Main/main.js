const CMD_FILE = "command.json";
const CMD_DIR = "/sdcard/yunGateway/";


// 显示控制台（防止被回收）
console.show();
console.log("文件监听服务已启动命令文件: " + CMD_FILE);

//设置主main引擎
const mainEngine = engines.myEngine();
const mainEngineId = mainEngine.id;

let cmdId = 0
let cmdTime = 0

function initializeMainEngine(){
    engines.all().forEach(engine => {
        let scriptPath = engine.getSource() + ''; // 转为字符串路径
        // 如果不是main.js且不是自己，则停止
        if (engine.id !== mainEngineId) {
            console.log("开始停止脚本:", scriptPath);
            engine.forceStop(); // 强制停止
        }});
}

// 主监听循环
setInterval(() => {
    let cmdFile = CMD_DIR + CMD_FILE
    if (!files.exists(cmdFile)) return;
    let rawData = files.read(cmdFile);
    if (!rawData) return;
    let cmd = JSON.parse(rawData.trim());
    cmdId = cmd.id
    let nextCmdTime = cmd.cmdTime
    let action = cmd.action
    let script = cmd.script
    if (nextCmdTime === cmdTime) return;

    if (action === "run"){
        let cmdScript = CMD_DIR + script
        let func = cmd.function;
        let deviceId = cmd.deviceId;
        let config = cmd.config
        cmdTime = nextCmdTime
        initializeMainEngine()
        sleep(3000)
        let runningEngine = engines.execScriptFile(cmdScript)
        sleep(2000)
        console.log("开始启动指令:"+ func);
        let params = cmdId+":" + deviceId + ":" + func+":" + config
        runningEngine.getEngine().emit("subTasks", params);
        //清空命令文件，等待下次接受
        files.write(cmdFile, "");
    }else if (action === "stopAction"){
        initializeMainEngine()
    }

}, 10000); // 每10秒检查一次

// 保持脚本运行的保活机制
setInterval(() => {}, 1000);