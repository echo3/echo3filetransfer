/**
 * File transfer root namespace object.  Components are contained directly in this namespace.
 * @namespace
 */
FileTransfer = { };

/**
 * Abstract base class for upload components.
 * 
 * @sp {Boolean} autoSend flag indicating whether upload should be sent automatically in response to a "ready" event 
 */
FileTransfer.AbstractUploadSelect = Core.extend(Echo.Component, {
    
    $static: {
        
        /**
         * Generates a new unique identifier for use by the upload process manager.
         * 
         * @return the generated identifier
         * @type String
         */
        generateId: function() {
            var out = "", i = 0, random;
            for (i = 0; i < 16; ++i) {
                random = (Math.round(Math.random() * 0x10000)).toString(16);
                out += "0000".substring(random.length) + random;
            }
            return out;
        }
    },
    
    $abstract: true,
    
    /**
     * Flag indicating whether the upload component is actively sending file data to the server.
     * @type Boolean
     */
    sending: false,
    
    /**
     * The upload process id.  Generated at construction.
     * @type String
     */
    processId: null,
    
    /**
     * Constructor.
     */
    $construct: function(data) {
        this.processId = FileTransfer.AbstractUploadSelect.generateId();
        Echo.Component.call(this, data);
    },
    
    /**
     * Notifies <code>uploadCancel</code> listeners that the upload operation has been canceled.
     * Performs no operation if an upload sending is not in progress.
     * Sets sending state to false.
     */
    cancel: function() {
        if (!this.sending) {
            return;
        }
        this.sending = false;
        this.fireEvent({ source: this, type: "uploadCancel" });
    },
    
    /**
     * Notifies <code>uploadComplete</code> listeners that the upload operation has completed.
     * Performs no operation if an upload sending is not in progress.
     * Sets sending state to false.
     */
    complete: function() { 
        if (!this.sending) {
            return;
        }
        this.sending = false;
        this.fireEvent({ source: this, type: "uploadComplete", data: this.processId });
    },
    
    /**
     * Notifies <code>uploadReady</code> listeners that the files to uploaded have been selected and are ready for uploading.
     * If the property <code>autoSend</code> is set, the <code>send()</code> method will be subsequently invoked. 
     */
    ready: function() {
        this.fireEvent({ source: this, type: "uploadReady" });
        if (this.render("autoSend"), true) {
            this.send();
        }
    },
    
    /**
     * Sends the upload.
     * Performs no operation if upload sending is already in progress.
     * Sets sending state to true.
     */
    send: function() {
        if (this.sending) {
            return;
        }
        this.sending = true;
        this.fireEvent({ source: this, type: "uploadSend" });
    }
});

/**
 * UploadSelect component.
 */
FileTransfer.UploadSelect = Core.extend(FileTransfer.AbstractUploadSelect, {
    
    $load: function() {
        Echo.ComponentFactory.registerType("FileTransfer.UploadSelect", this);
    },
    
    /** @see Echo.Component#componentType */
    componentType: "FileTransfer.UploadSelect"
});
