/**
 * UploadSelect component.
 */
FileTransfer.UploadSelect = Core.extend(Echo.Component, {
    
    $load: function() {
        Echo.ComponentFactory.registerType("FileTransfer.UploadSelect", this);
    },
    
    componentType: "FileTransfer.UploadSelect"
});
