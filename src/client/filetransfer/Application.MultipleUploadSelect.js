/**
 * UploadSelect component.
 */
FileTransfer.MultipleUploadSelect = Core.extend(FileTransfer.AbstractUploadSelect, {
    
    $load: function() {
        Echo.ComponentFactory.registerType("FileTransfer.MultipleUploadSelect", this);
    },

    /** @see Echo.Component#componentType */
    componentType: "FileTransfer.MultipleUploadSelect"
});
