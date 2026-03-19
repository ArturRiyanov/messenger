const { contextBridge } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
    // сюда можно добавлять нативные функции позже
});
