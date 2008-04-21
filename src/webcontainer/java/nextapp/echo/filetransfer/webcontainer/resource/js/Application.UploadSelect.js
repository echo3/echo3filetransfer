/**
 * @class UploadSelect component.
 * @base Echo.Component
 */
FileTransfer.UploadSelect = Core.extend(Echo.Component, {
    
    $load: function() {
        Echo.ComponentFactory.registerType("nextapp.echo.filetransfer.app.UploadSelect", this);
    },
    
    componentType: "nextapp.echo.filetransfer.app.UploadSelect"
});