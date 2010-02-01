/**
 * Component rendering peer: MultipleUploadSelect.
 */
FileTransfer.Sync.MultipleUploadSelect = Core.extend(FileTransfer.Sync.UploadSelect, {

    $load: function() {
        Echo.Render.registerPeer("FileTransfer.MultipleUploadSelect", this);
        
        if (Core.Web.Env.ENGINE_MSHTML) {
            try {
                this.flashAvailable = !!(new ActiveXObject("ShockwaveFlash.ShockwaveFlash.9")); 
            } catch (ex) { }
        } else if (navigator.plugins) {
            for (var i = 0; i < navigator.plugins.length; ++i) {
                if (/flash/gi.test(navigator.plugins[i].name)) {
                    var majorVersionMatch = /[0-9]+/g.exec(navigator.plugins[i].description);
                    if (majorVersionMatch) {
                        var version = parseInt(majorVersionMatch[0], 10);
                        this.flashAvailable = version >= 9;
                    }
                }
            }
        }
    },
    
    $static: {
        
        /**
         * Flag indicating whether a suitable version of Flash (i.e., 9.0+) is available.
         * @type Boolean
         */
        flashAvailable: false
    },
    
    /** @see Echo.Render.ComponentSync#renderAdd */
    renderAdd: function(update, parentElement) {
        if (FileTransfer.Sync.MultipleUploadSelect.flashAvailable && !this.uploadRender) {
            this.uploadRender = new FileTransfer.Sync.MultipleUploadSelect.SWFUploadRender(); 
        }
        FileTransfer.Sync.UploadSelect.prototype.renderAdd.call(this, update, parentElement);
    }
});

/**
 * <code>FileTransfer.Sync.UploadRender</code> implementation for SWFUpload component.
 */
FileTransfer.Sync.MultipleUploadSelect.SWFUploadRender = Core.extend(FileTransfer.Sync.UploadRender, {

    _buttonDiv: null,
    _swfUploadDiv: null,
    _swfUpload: null,
    _progressDisplay: null,
    _progressFileMap: null,
    _canceled: false,
    
    /** @see FileTransfer.Sync.UploadRender#add */
    add: function() {
        this.peer.div.style.cssText = "position:relative;overflow:hidden;width:" + Echo.Sync.Extent.toPixels("20em") + "px;";
        if (Core.Web.Env.QUIRK_IE_HAS_LAYOUT) {
            this.peer.div.style.zoom = 1;
        }

        this._buttonDiv = document.createElement("div");
        Echo.Sync.renderComponentDefaults(this.peer.component, this._buttonDiv);
        Echo.Sync.Border.render(this.peer.component.render("border"), this._buttonDiv);
        Echo.Sync.Insets.render(this.peer.component.render("insets"), this._buttonDiv, "padding");
        this._buttonDiv.appendChild(document.createTextNode(this.peer.component.render("text", "Upload")));
        this.peer.div.appendChild(this._buttonDiv);
    },
  
    /** @see FileTransfer.Sync.UploadRender#add */
    cancel: function() {
        this._canceled = true;
        this._swfUpload.cancelUpload();
    },
    
    /** @see FileTransfer.Sync.UploadRender#display */
    display: function() {
        var bounds = new Core.Web.Measure.Bounds(this.peer.div, { flags: Core.Web.Measure.Bounds.FLAG_MEASURE_DIMENSION });
        if (!this._swfUpload) {
            this._renderUploadControl(bounds);
        }
    },
    
    /** @see FileTransfer.Sync.UploadRender#dispose */
    dispose: function() {
        this._progressFileMap = null;
        Core.Debug.consoleWrite(this._swfUpload.destroy());
        this._swfUploadDiv = null;
    },

    /**
     * Starts upload of next queued file.
     */
    _processNextUpload: function() {
        if (this._canceled) {
            return;
        }
        var stats = this._swfUpload.getStats();
        if (stats.in_progress) {
            throw new Error("Attempt to initiate upload while upload in progress.");
        }
        if (stats.files_queued) {
            this._swfUpload.startUpload();
        } else {
            this.peer.component.complete();
        }
    },
    
    /**
     * SWFUpload <code>file_dialog_complete_handler</code> event method.
     */
    _processDialogComplete: function(selected, queued, total) {
        if (this._swfUpload.getStats().files_queued) {
            this.peer.component.ready();
        }
    },

    /**
     * SWFUpload <code>upload_start_handler</code> event method.
     */
    _processUploadStart: function(e) {
        Core.Debug.consoleWrite("Upload started:" + e);
    },
    
    /**
     * SWFUpload <code>upload_progress_handler</code> event method.
     */
    _processUploadComplete: function(e) {
        this._processNextUpload();
        Core.Debug.consoleWrite("complete:" + Core.Debug.toString(arguments)) ;
        Core.Debug.consoleWrite(this._swfUpload.startUpload());
    },
    
    /**
     * SWFUpload <code>upload_success_handler</code> event method.
     */
    _processProgress: function(file, progress, size) {
        var progressFile = this._progressFileMap[file.id];
        progressFile.progress = progress;
        this.peer.progressDisplay.update();
    },

    /**
     * Renders the SWFUpload control with the specified bounds.
     */
    _renderUploadControl: function(bounds) {
        this._swfUploadDiv = document.createElement("div");
        this._swfUploadDiv.style.cssText = "position:absolute;top:0;left:0;width:100%;height:100%;";
        this.peer.div.appendChild(this._swfUploadDiv);
        
        var span = document.createElement("span");
        this._swfUploadDiv.appendChild(span);
        
        var uploadUrl = this.peer.component.get("receiver");
        uploadUrl += (uploadUrl.indexOf("?") == -1 ? "?" : "&") + this.peer.urlParameters.join("&");
        
        this._swfUpload = new SWFUpload({
            button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,
            button_placeholder: span,
            button_width: bounds.width,
            button_height: bounds.height,
            button_text: "",
            preserve_relative_urls: true,
            upload_url: uploadUrl,
            flash_url: this.peer.client.getResourceUrl("FileTransfer", "swfupload.swf"),
            file_size_limit: this.peer.component.render("maximumSize", 20 * 1024 * 1024),
            upload_start_handler: Core.method(this, this._processUploadStart),
            upload_success_handler: Core.method(this, this._processUploadComplete),
            upload_progress_handler: Core.method(this, this._processProgress),
            file_dialog_complete_handler: Core.method(this, this._processDialogComplete)
        });
    },
    
    /** @see FileTransfer.Sync.UploadRender#send */
    send: function() {
        this._swfUpload.setButtonDimensions(0, 0);
        this.peer.div.style.width = "";
        this._buttonDiv.style.display = "none";
        
        this._progressFileMap = {};
        var fileCount = this._swfUpload.getStats().files_queued;
        for (var i = 0; i < fileCount; ++i) {
            var file = this._swfUpload.getFile(i);
            var progressFile = new FileTransfer.Sync.ProgressDisplay.File(file.name, file.size);
            this._progressFileMap[file.id] = progressFile;
            this.peer.progressDisplay.add(progressFile);
        }
        this.peer.progressDisplay.init();
        this._processNextUpload();
    }
});
