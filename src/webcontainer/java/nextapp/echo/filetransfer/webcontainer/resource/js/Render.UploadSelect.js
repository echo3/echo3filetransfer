/**
 * Component rendering peer: UploadSelect
 */
FileTransfer.Sync.UploadSelect = Core.extend(Echo.Render.ComponentSync, {

    $static: {
		_STAGE_LOADED: 1,
		_STAGE_QUEUED: 2,
		_STAGE_UPLOADING: 3,

		/**
		 * Global registration of all UploadSelect components, because most browsers allow ~2 connections per server.
		 * If we would allow multiple uploads it would not be possible anymore to perform UI-interaction.
		 */
		_activeUploads: 0,
		_uploadSelects: [],
		
		/**
		 * Service URIs.
		 */
		_blankWindowService: "?sid=EchoFileTransfer.BlankWindowService",
		_progressService: "?sid=EchoFileTransfer.UploadProgressService",
		_receiverService: "?sid=EchoFileTransfer.UploadReceiverService",
		
		_defaultProgressInterval: 1000,
		
		_defaultHeight: "50px",
		_defaultWidth: "275px",
		
		_register: function(uploadSelect) {
			this._uploadSelects.push(uploadSelect);
		},
		
		_deregister: function(uploadSelect) {
			Core.Arrays.remove(this._uploadSelects, uploadSelect);
			this._startNextUpload();
		},
		
		_hasUploadSlot: function() {
			return this._activeUploads == 0;
		},
		
		_startNextUpload: function() {
			if (!this._hasUploadSlot()) {
				return;
			}
			var uploadSelects = this._uploadSelects;
			for (var i = 0; i < uploadSelects.length; i++) {
				if (uploadSelects[i]._startNextUpload()) {
					return;
				}
			}
		}
    },
    
    $load: function() {
        Echo.Render.registerPeer("nextapp.echo.filetransfer.app.UploadSelect", this);
    },
    
    $construct: function() {
		this._divElement = null;
		this._uploadId = -1;
		this._activeUploads = 0;
		this._frames = new Object();
    },

	renderAdd: function(update, parentElement) {
		FileTransfer.Sync.UploadSelect._register(this);
		
		this._uploadId = this.component.get("uploadIndex");
		
		this._divElement = document.createElement("div");
		Echo.Sync.Color.render(this.component.render("background"), this._divElement, "backgroundColor");
		parentElement.appendChild(this._divElement);
		
		this._addFrame();
	},
	
	/**
	 * Adds a new upload frame.
	 */
	_addFrame: function() {
		var uploadId = ++this._uploadId;
		var frame = new FileTransfer.Sync.UploadSelect.Frame(this.component, uploadId);
		frame._renderAdd(this._divElement);
		this._frames[uploadId] = frame;
	},
	
	/**
	 * Removes the given upload frame.
	 * 
	 * @param frame {FileTransfer.Sync.UploadSelect.Frame}
	 */
	_removeFrame: function(frame) {
		delete this._frames[frame._uploadId];
		frame._dispose();
	},
	
	/**
	 * Starts one of the queued uploads if there are any.
	 * 
	 * @return <tt>true</tt> if an upload was started.
	 * @type Boolean
	 */
	_startNextUpload: function() {
		for (var uploadId in this._frames) {
			var frame = this._frames[uploadId];
			if (frame._loadStage == FileTransfer.Sync.UploadSelect._STAGE_QUEUED) {
				FileTransfer.Sync.UploadSelect._activeUploads++;
				this._activeUploads++;
				frame._startUpload();
				return true;
			}
		}
		return false;
	},
	
	renderUpdate: function(update) {
	    var canceledUploadsUpdate = update.getUpdatedProperty("uploadCanceled");
	    // we should be checking if this is the only update, but if we do a full render all uploads are aborted
	    if (canceledUploadsUpdate) {
	    	var canceledUploads = canceledUploadsUpdate.newValue.split(",");
	    	for (var i = 0; i < canceledUploads.length; i++) {
	    		var frame = this._frames[canceledUploads[i]];
	    		if (frame) {
		    		frame._processCancel();
	    		}
	    	}
	    	return false;
	    }
	    var element = this._divElement;
	    var containerElement = element.parentNode;
	    this.renderDispose(update);
	    containerElement.removeChild(element);
	    this.renderAdd(update, containerElement);
	    return false;
	},
	
	renderDispose: function(update) {
		FileTransfer.Sync.UploadSelect._activeUploads -= this._activeUploads;
		
		this._divElement = null;
		for (var uploadId in this._frames) {
			this._frames[uploadId]._dispose();
		}
		this._activeUploads = 0;
		this._frames = new Object();
	
		FileTransfer.Sync.UploadSelect._deregister(this);
	}
});

/**
 * Represents an upload frame consisting of the actual file input and optionally a submit button.
 */
FileTransfer.Sync.UploadSelect.Frame = Core.extend({
	
	/**
	 * @param component {Echo.Component}
	 * @param uploadId {Number} the upload index
	 */
	$construct: function(component, uploadId) {
		this.component = component;
		this._uploadId = uploadId;
		this._loadStage = null;
		this._frameElement = null;
		this._formElement = null;
		this._fileElement = null;
		this._submitElement = null;
		this._submitListenerBound = false;
	},
	
	_renderAdd: function(parentElement) {
		this._frameElement = document.createElement("iframe");
		// id needed for Safari, otherwise multiple iframes do not load
		this._frameElement.id = this.component.renderId + "_frame" + this._uploadId;
		this._frameElement.src = FileTransfer.Sync.UploadSelect._blankWindowService;
		this._frameElement.scrolling = "no";
		this._frameElement.frameBorder = "0";
		
		var width = this.component.render("width", FileTransfer.Sync.UploadSelect._defaultWidth);
	   	this._frameElement.style.width = Echo.Sync.Extent.toCssValue(width, true);
	   	
		var height = this.component.render("height", FileTransfer.Sync.UploadSelect._defaultHeight);
	   	this._frameElement.style.height = Echo.Sync.Extent.toCssValue(height, false);
		
		var processLoad = Core.method(this, this._processLoad);
		if (Core.Web.Env.BROWSER_INTERNET_EXPLORER) {
		    Core.Web.Event.add(this._frameElement, "load", processLoad, false);
		} else {
			this._frameElement.onload = processLoad;
		}
		
		parentElement.appendChild(this._frameElement);
	},
	
	/**
	 * Starts this upload and starts the progress poller.
	 */
	_startUpload: function() {
		this._loadStage = FileTransfer.Sync.UploadSelect._STAGE_UPLOADING;
		this._formElement.submit();
		
		if (!this.component.render("queueEnabled")) {
			if (!Core.Web.Env.BROWSER_SAFARI) {
				// Safari refuses to upload when file element is disabled
				this._fileElement.disabled = true;
			}
		}
		this._startProgressPoller();
	},
	
	/**
	 * Instructs this frame that the upload has ended.
	 */
	_uploadEnded: function() {
		this._stopProgressPoller();
		var peer = this.component.peer;
		if (this._loadStage == FileTransfer.Sync.UploadSelect._STAGE_UPLOADING) {
			peer._activeUploads--;
			FileTransfer.Sync.UploadSelect._activeUploads--;
			FileTransfer.Sync.UploadSelect._startNextUpload();
		}
		var queueEnabled = this.component.render("queueEnabled");
		peer._removeFrame(this);
		if (!queueEnabled) {
			peer._addFrame();
		}
	},
	
	_pollProgress: function() {
		if (!this._enableProgressPoll) {
			return;
		}
	    var conn = new Core.Web.HttpConnection(this._createProgressUrl(), "GET", null, null);
	    conn.addResponseListener(Core.method(this, this._processProgressResponse));
	    conn.connect();
	},
	
	_processProgressResponse: function(e) {
		if (this._enableProgressPoll) {
			this._startProgressPoller();
		}
	},
	
	_startProgressPoller: function() {
		this._enableProgressPoll = true;
		var interval = this.component.render("progressInterval", FileTransfer.Sync.UploadSelect._defaultProgressInterval);
		Core.Web.Scheduler.run(Core.method(this, this._pollProgress), interval, false);
	},
	
	_stopProgressPoller: function() {
		this._enableProgressPoll = false;
	},
	
	_processCancel: function() {
		this._uploadEnded();
	},
	
	_processSubmit: function(e) {
		if (e) {
			Core.Web.DOM.preventEventDefault(e);
		}
		
		if (!this._fileElement.value || this._fileElement.value == "") {
			return;
		}
	    
	    this._loadStage = FileTransfer.Sync.UploadSelect._STAGE_QUEUED;
	    // remove listener before document gets disposed
	    Core.Web.Event.remove(this._formElement, "submit", Core.method(this, this._processSubmit), false);
	    this._submitListenerBound = false;
		
		if (this.component.render("queueEnabled")) {
			this._frameElement.style.width = "0px";
			this._frameElement.style.height = "0px";
			this.component.peer._addFrame();
		} else if (this._submitElement) {
			this._submitElement.disabled = true;
			var text = this.component.render("sendButtonWaitText");
			if (text) {
				this._submitElement.value = text;
			}
		}
		
		FileTransfer.Sync.UploadSelect._startNextUpload();
	},
	
	_processLoad: function() {
		if (this._loadStage) {
			this._uploadEnded();
			return;
		}
		this._loadStage = FileTransfer.Sync.UploadSelect._STAGE_LOADED;
		
		var frameDocument = this._frameElement.contentWindow.document;
		
		var body = frameDocument.getElementsByTagName("body")[0];
		body.style.border = "none";
		body.style.margin = "0px";
		body.style.padding = "0px";
		Echo.Sync.Color.render(this.component.render("background"), body, "backgroundColor");
		
		if (Core.Web.Env.BROWSER_INTERNET_EXPLORER) {
	        this._formElement = frameDocument.createElement("<form enctype='multipart/form-data'/>");
		} else {
			this._formElement = frameDocument.createElement("form");
			this._formElement.enctype = "multipart/form-data";
		}
		this._formElement.action = this._createUploadUrl();
		this._formElement.method = "POST";
		this._formElement.style.margin = "0px";
		this._formElement.style.padding = "0px";
		
	    Core.Web.Event.add(this._formElement, "submit", Core.method(this, this._processSubmit)); 
		this._submitListenerBound = true;
		
		var browseBackgroundImage = this.component.render("browseButtonBackgroundImage");
		var browseRolloverBackgroundImage = this.component.render("browseButtonRolloverBackgroundImage");
		var browseText = this.component.render("browseButtonText");
		var browseWidth = this.component.render("browseButtonWidth");
		var browsePixelWidth;
		if (browseWidth) {
			browsePixelWidth = Echo.Sync.Extent.toPixels(browseWidth, true);
		}
		var browseHeight = this.component.render("browseButtonHeight");
		
		this._fileElement = frameDocument.createElement("input");
		this._fileElement.name = this.component.renderId + "_file_" + this._uploadId;
		this._fileElement.type = "file";
		
		var fileSelectorWidth = this.component.render("fileSelectorWidth");
		if (!fileSelectorWidth) {
			fileSelectorWidth = this.component.render("width", FileTransfer.Sync.UploadSelect._defaultWidth);
		}
		var fileSelectorPixelWidth = Echo.Sync.Extent.toPixels(fileSelectorWidth, true);
		
		if (browseBackgroundImage || browseRolloverBackgroundImage || browseText || browseWidth || browseHeight) {
			// use styling hack (http://www.quirksmode.org/dom/inputfile.html)
			this._fileElement.onchange = function() {overlayInput.value = this.value;};
			this._fileElement.onkeydown = function() {overlayInput.value = this.value;};
			this._fileElement.onkeyup = function() {overlayInput.value = this.value;};
			if (Core.Web.Env.BROWSER_MOZILLA) {
				body.appendChild(this._fileElement);
				var newSize = 1;
				this._fileElement.size = newSize;
				while (newSize < 1000 && this._fileElement.offsetWidth < fileSelectorPixelWidth) {
					newSize++;
					this._fileElement.size = newSize;
				}
				body.removeChild(this._fileElement);
			} else {
				this._fileElement.style.width = fileSelectorPixelWidth + "px";
			}
			
			if (Core.Web.Env.PROPRIETARY_IE_OPACITY_FILTER_REQUIRED) {
				this._fileElement.style.filter = "alpha(opacity: 0)";
			} else {
				this._fileElement.style.opacity = "0";
			}
			
			var outerDiv = frameDocument.createElement("div");
			outerDiv.style.position = "relative";
			
			var hiddenFileDiv = frameDocument.createElement("div");
			hiddenFileDiv.style.position = "relative";
			hiddenFileDiv.style.zIndex = "2";
			hiddenFileDiv.appendChild(this._fileElement);
			outerDiv.appendChild(hiddenFileDiv);
			
			var overlayFileDiv = frameDocument.createElement("div");
			overlayFileDiv.style.position = "absolute";
			overlayFileDiv.style.top = "0px";
			overlayFileDiv.style.left = "0px";
			overlayFileDiv.style.zIndex = "1";
			
			var overlayInput = frameDocument.createElement("input");
			overlayInput.type = "text";
			overlayInput.style[Core.Web.Env.CSS_FLOAT] = "left";
			Echo.Sync.Color.render(this.component.render("foreground"), overlayInput, "color");
			Echo.Sync.Font.render(this.component.render("font"), overlayInput);
			overlayFileDiv.appendChild(overlayInput);
			
			var overlayBrowse;
			if (browseBackgroundImage || browseRolloverBackgroundImage) {
				overlayBrowse = frameDocument.createElement("div");
				overlayBrowse.style.textAlign = "center";
				if (browseBackgroundImage) {
					Echo.Sync.FillImage.render(browseBackgroundImage, overlayBrowse);
				}
				if (browseRolloverBackgroundImage) {
					this._fileElement.onmouseover = function() {Echo.Sync.FillImage.renderClear(browseRolloverBackgroundImage, overlayBrowse);};
					this._fileElement.onmouseout = function() {Echo.Sync.FillImage.renderClear(browseBackgroundImage, overlayBrowse);};
				}
			} else {
				overlayBrowse = frameDocument.createElement("button");
			}
			overlayBrowse.style[Core.Web.Env.CSS_FLOAT] = "right";
			overlayBrowse.appendChild(frameDocument.createTextNode(browseText ? browseText : ">>"));
			var overlayInputWidth;
			if (browseWidth) {
				overlayBrowse.style.width = browsePixelWidth + "px";
				overlayInputWidth = fileSelectorPixelWidth - browsePixelWidth;
			} else {
				overlayInputWidth = fileSelectorPixelWidth - 75;
			}
			// compensate for input/button spacing
			overlayInputWidth = overlayInputWidth - 2;
			if (overlayInputWidth > 0) {
				overlayInput.style.width = overlayInputWidth + "px";
			} else {
				overlayInput.style.width = "0px";
				overlayInput.style.display = "none";
				if (Core.Web.Env.BROWSER_MOZILLA) {
					this._fileElement.style.marginLeft = "-32px";
				}
			}
			if (browseHeight) {
				overlayBrowse.style.height = Echo.Sync.Extent.toPixels(browseHeight, false) + "px";
			}
			Echo.Sync.Color.render(this.component.render("foreground"), overlayBrowse, "color");
			Echo.Sync.Font.render(this.component.render("font"), overlayBrowse);
			overlayFileDiv.appendChild(overlayBrowse);
			outerDiv.appendChild(overlayFileDiv);
			
			this._formElement.appendChild(outerDiv);
		} else {
			Echo.Sync.Color.render(this.component.render("foreground"), this._fileElement, "color");
			Echo.Sync.Font.render(this.component.render("font"), this._fileElement);
			this._formElement.appendChild(this._fileElement);
		}
		
		if (this.component.render("sendButtonDisplayed", true)) {
			this._submitElement = frameDocument.createElement("input");
			this._submitElement.type = "submit";
			this._submitElement.style.marginTop = "3px";
			var sendText = this.component.render("sendButtonText");
			if (sendText) {
				this._submitElement.value = sendText;
			}
			var sendWidth = this.component.render("sendButtonWidth");
			if (sendWidth) {
				this._submitElement.style.width = Echo.Sync.Extent.toPixels(sendWidth, true) + "px";
			}
			var sendHeight = this.component.render("sendButtonHeight");
			if (sendHeight) {
				this._submitElement.style.height = Echo.Sync.Extent.toPixels(sendHeight, false) + "px";
			}
			
			Echo.Sync.Font.render(this.component.render("font"), this._submitElement);
			Echo.Sync.Color.render(this.component.render("foreground"), this._submitElement, "color");
			this._formElement.appendChild(this._submitElement);
		} else {
			var instance = this;
			this._fileElement.onchange = function(e) {
				instance._processSubmit(e);
			};
		}
		
		body.appendChild(this._formElement);
	},
	
	_createUploadUrl: function() {
		return FileTransfer.Sync.UploadSelect._receiverService + "&i=" + this.component.renderId + "&x=" + this._uploadId;
	},
	
	_createProgressUrl: function() {
		return FileTransfer.Sync.UploadSelect._progressService + "&i=" + this.component.renderId + "&x=" + this._uploadId;
	},
	
	_dispose: function() {
		this._stopProgressPoller();
		if (this._submitListenerBound) {
		    Core.Web.Event.remove(this._formElement, "submit", Core.method(this, this._processSubmit), false);
			this._submitListenerBound = false;
		}
		if (Core.Web.Env.BROWSER_INTERNET_EXPLORER) {
		    Core.Web.Event.remove(this._frameElement, "load", Core.method(this, this._processLoad), false);
		} else {
			this._frameElement.onload = null;
		}
		if (this._loadStage == FileTransfer.Sync.UploadSelect._STAGE_UPLOADING) {
		    // gracefully stop upload
		    var frameWindow = this._frameElement.contentWindow;
			if (frameWindow.stop) {
		    	frameWindow.stop();
			} else if (frameWindow.document && frameWindow.document.execCommand) {
				frameWindow.document.execCommand("Stop");
			} else {
				frameWindow.location.href = FileTransfer.Sync.UploadSelect._blankWindowService;
			}
		}
		if (Core.Web.Env.BROWSER_MOZILLA) {
			// bypass waiting forever bug
			var frame = this._frameElement;
			setTimeout(function() {
				frame.parentNode.removeChild(frame);
			}, 0);
		} else {
			this._frameElement.parentNode.removeChild(this._frameElement);
		}
		this.component = null;
		this._uploadId = null;
		this._loadStage = null;
		this._frameElement = null;
		this._formElement = null;
		this._fileElement = null;
		this._submitElement = null;
	}
});