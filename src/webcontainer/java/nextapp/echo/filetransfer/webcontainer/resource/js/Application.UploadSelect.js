/**
 * @class UploadSelect component.
 * @base EchoApp.Component
 */
ExtrasApp.UploadSelect = Core.extend(EchoApp.Component, {
    
    $load: function() {
        EchoApp.ComponentFactory.registerType("nextapp.echo.filetransfer.app.UploadSelect", this);
    },
    
    componentType: "nextapp.echo.filetransfer.app.UploadSelect"
});