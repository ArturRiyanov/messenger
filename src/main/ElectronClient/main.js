const { app, BrowserWindow } = require('electron');
const path = require('path');

let mainWindow;

function createWindow() {
    mainWindow = new BrowserWindow({
        width: 1320,
        height: 880,
        minWidth: 1024,
        minHeight: 700,
        backgroundColor: '#020617',
        title: 'Messenger',
        webPreferences: {
            preload: path.join(__dirname, 'preload.js'),
            nodeIntegration: false,
            contextIsolation: true
        }
    });

    // Загружаем твой index.html
    mainWindow.loadFile(path.join(__dirname, 'static', 'index.html'));

    // Отключить меню (по желанию)
    // mainWindow.setMenu(null);

    // DevTools (включи если нужно)
    // mainWindow.webContents.openDevTools();

    mainWindow.on('closed', () => {
        mainWindow = null;
    });
}

app.whenReady().then(() => {
    createWindow();

    app.on('activate', () => {
        if (BrowserWindow.getAllWindows().length === 0) createWindow();
    });
});

app.on('window-all-closed', () => {
    if (process.platform !== 'darwin') app.quit();
});
