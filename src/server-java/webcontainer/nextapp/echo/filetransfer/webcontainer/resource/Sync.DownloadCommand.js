/**
 * Command exeecution peer: Download
 */
Echo.RemoteClient.CommandExec.Download = {

    execute: function(client, commandData) {
        if (!commandData.uri) {
            throw new Error("uri not specified in DownloadCommand.");
        }
        top.location = commandData.uri;
    }    
};

Echo.RemoteClient.CommandExecProcessor.registerPeer("nextapp.echo.filetransfer.app.DownloadCommand", 
        Echo.RemoteClient.CommandExec.Download);