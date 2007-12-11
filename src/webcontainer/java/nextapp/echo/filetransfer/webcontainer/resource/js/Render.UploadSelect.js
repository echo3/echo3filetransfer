/**
 * Component rendering peer: UploadSelect
 */
FileTransferRender.UploadSelectSync = Core.extend(EchoRender.ComponentSync, {

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
		
		_defaultHeight: new EchoApp.Extent("50px"),
		_defaultWidth: new EchoApp.Extent("275px"),
		
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
		},
		
		/**
		 * Strips the given file name of any path information.
		 * 
		 * @param fileName {String} the file name
		 * @return the stripped file name.
		 * @type String
		 */
		_stripFileName: function(fileName) {
			var index = fileName.lastIndexOf('/');
			if (index != -1) {
				fileName = fileName.substring(index + 1);
			}
			index = fileName.lastIndexOf('\\');
			if (index != -1) {
				fileName = fileName.substring(index + 1);
			}
			return fileName;
		}
    },
    
    $load: function() {
        EchoRender.registerPeer("nextapp.echo.filetransfer.app.UploadSelect", this);
    },
    
    $construct: function() {
		this._divElement = null;
		this._uploadId = -1;
		this._activeUploads = 0;
		this._frames = new Object();
    },

	renderAdd: function(update, parentElement) {
		FileTransferRender.UploadSelectSync._register(this);
		
		this._uploadId = this.component.getProperty("uploadIndex");
		
		this._divElement = document.createElement("div");
		EchoAppRender.Color.renderComponentProperty(this.component, "background", null, this._divElement, "backgroundColor");
		parentElement.appendChild(this._divElement);
		
		this._addFrame();
	},
	
	/**
	 * Adds a new upload frame.
	 */
	_addFrame: function() {
		var uploadId = ++this._uploadId;
		var frame = new FileTransferRender.UploadSelectSync.Frame(this.component, uploadId);
		frame._renderAdd(this._divElement);
		this._frames[uploadId] = frame;
	},
	
	/**
	 * Removes the given upload frame.
	 * 
	 * @param frame {FileTransferRender.UploadSelectSync.Frame}
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
			if (frame._loadStage == FileTransferRender.UploadSelectSync._STAGE_QUEUED) {
				FileTransferRender.UploadSelectSync._activeUploads++;
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
		FileTransferRender.UploadSelectSync._activeUploads -= this._activeUploads;
		
		this._divElement = null;
		for (var uploadId in this._frames) {
			this._frames[uploadId]._dispose();
		}
		this._activeUploads = 0;
		this._frames = new Object();
	
		FileTransferRender.UploadSelectSync._deregister(this);
	}
});

/**
 * Represents an upload frame consisting of the actual file input and optionally a submit button.
 */
FileTransferRender.UploadSelectSync.Frame = Core.extend({
	
	/**
	 * @param component {EchoApp.Component}
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
		this._frameElement.src = FileTransferRender.UploadSelectSync._blankWindowService;
		this._frameElement.scrolling = "no";
		this._frameElement.frameBorder = "0";
		
		var width = this.component.getRenderProperty("width", FileTransferRender.UploadSelectSync._defaultWidth);
	   	this._frameElement.style.width = EchoAppRender.Extent.toPixels(width, true) + "px";
	   	
		var height = this.component.getRenderProperty("height", FileTransferRender.UploadSelectSync._defaultHeight);
	   	this._frameElement.style.height = EchoAppRender.Extent.toPixels(height, false) + "px";
		
		var processLoad = Core.method(this, this._processLoad);
		if (WebCore.Environment.BROWSER_INTERNET_EXPLORER) {
		    WebCore.EventProcessor.add(this._frameElement, "load", processLoad, false);
		} else {
			this._frameElement.onload = processLoad;
		}
		
		parentElement.appendChild(this._frameElement);
	},
	
	/**
	 * Starts this upload and starts the progress poller.
	 */
	_startUpload: function() {
		this._loadStage = FileTransferRender.UploadSelectSync._STAGE_UPLOADING;
		this._formElement.submit();
		
		if (!this.component.getRenderProperty("queueEnabled")) {
			if (!WebCore.Environment.BROWSER_SAFARI) {
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
		if (this._loadStage == FileTransferRender.UploadSelectSync._STAGE_UPLOADING) {
			peer._activeUploads--;
			FileTransferRender.UploadSelectSync._activeUploads--;
			FileTransferRender.UploadSelectSync._startNextUpload();
		}
		var queueEnabled = this.component.getRenderProperty("queueEnabled");
		peer._removeFrame(this);
		if (!queueEnabled) {
			peer._addFrame();
		}
	},
	
	_pollProgress: function() {
		if (!this._enableProgressPoll) {
			return;
		}
	    var conn = new WebCore.HttpConnection(this._createProgressUrl(), "GET", null, null);
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
		var interval = this.component.getRenderProperty("progressInterval", FileTransferRender.UploadSelectSync._defaultProgressInterval);
		Core.Scheduler.run(Core.method(this, this._pollProgress), interval, false);
	},
	
	_stopProgressPoller: function() {
		this._enableProgressPoll = false;
	},
	
	_processCancel: function() {
		this._uploadEnded();
	},
	
	_processSubmit: function(e) {
		if (e) {
			WebCore.DOM.preventEventDefault(e);
		}
		
		var fileName = FileTransferRender.UploadSelectSync._stripFileName(this._fileElement.value);
		if (!fileName || fileName == "") {
			return;
		}
	    
	    this._loadStage = FileTransferRender.UploadSelectSync._STAGE_QUEUED;
	    this._formElement.action += "&name=" + fileName;
	    // remove listener before document gets disposed
	    WebCore.EventProcessor.remove(this._formElement, "submit", Core.method(this, this._processSubmit), false);
	    this._submitListenerBound = false;
		
		if (this.component.getRenderProperty("queueEnabled")) {
			this._frameElement.style.width = "0px";
			this._frameElement.style.height = "0px";
			this.component.peer._addFrame();
		} else if (this._submitElement) {
			this._submitElement.disabled = true;
			var text = this.component.getRenderProperty("sendButtonWaitText");
			if (text) {
				this._submitElement.value = text;
			}
		}
		
		FileTransferRender.UploadSelectSync._startNextUpload();
	},
	
	_processLoad: function() {
		if (this._loadStage) {
			this._uploadEnded();
			return;
		}
		this._loadStage = FileTransferRender.UploadSelectSync._STAGE_LOADED;
		
		var frameDocument = this._frameElement.contentWindow.document;
		
		var body = frameDocument.getElementsByTagName("body")[0];
		body.style.border = "none";
		body.style.margin = "0px";
		body.style.padding = "0px";
		EchoAppRender.Color.renderComponentProperty(this.component, "background", null, body, "backgroundColor");
		
		if (WebCore.Environment.BROWSER_INTERNET_EXPLORER) {
	        this._formElement = frameDocument.createElement("<form enctype='multipart/form-data'/>");
		} else {
			this._formElement = frameDocument.createElement("form");
			this._formElement.enctype = "multipart/form-data";
		}
		this._formElement.action = this._createUploadUrl();
		this._formElement.method = "POST";
		this._formElement.style.margin = "0px";
		this._formElement.style.padding = "0px";
		
	    WebCore.EventProcessor.add(this._formElement, "submit", Core.method(this, this._processSubmit)); 
		this._submitListenerBound = true;
		
		var browseBackgroundImage = this.component.getRenderProperty("browseButtonBackgroundImage");
		var browseRolloverBackgroundImage = this.component.getRenderProperty("browseButtonRolloverBackgroundImage");
		var browseText = this.component.getRenderProperty("browseButtonText");
		var browseWidth = this.component.getRenderProperty("browseButtonWidth");
		var browsePixelWidth;
		if (browseWidth) {
			browsePixelWidth = EchoAppRender.Extent.toPixels(browseWidth, true);
		}
		var browseHeight = this.component.getRenderProperty("browseButtonHeight");
		
		this._fileElement = frameDocument.createElement("input");
		this._fileElement.name = this.component.renderId + "_file_" + this._uploadId;
		this._fileElement.type = "file";
		
		var fileSelectorWidth = this.component.getRenderProperty("fileSelectorWidth");
		if (!fileSelectorWidth) {
			fileSelectorWidth = this.component.getRenderProperty("width", FileTransferRender.UploadSelectSync._defaultWidth);
		}
		var fileSelectorPixelWidth = EchoAppRender.Extent.toPixels(fileSelectorWidth, true);
		
		if (browseBackgroundImage || browseRolloverBackgroundImage || browseText || browseWidth || browseHeight) {
			// use styling hack (http://www.quirksmode.org/dom/inputfile.html)
			this._fileElement.onchange = function() {overlayInput.value = this.value;};
			this._fileElement.onkeydown = function() {overlayInput.value = this.value;};
			this._fileElement.onkeyup = function() {overlayInput.value = this.value;};
			if (WebCore.Environment.BROWSER_MOZILLA) {
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
			
			if (WebCore.Environment.PROPRIETARY_IE_OPACITY_FILTER_REQUIRED) {
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
			overlayInput.style[WebCore.Environment.CSS_FLOAT] = "left";
			EchoAppRender.Color.renderComponentProperty(this.component, "foreground", null, overlayInput, "color");
			EchoAppRender.Font.renderDefault(this.component, overlayInput);
			overlayFileDiv.appendChild(overlayInput);
			
			var overlayBrowse;
			if (browseBackgroundImage || browseRolloverBackgroundImage) {
				overlayBrowse = frameDocument.createElement("div");
				overlayBrowse.style.textAlign = "center";
				if (browseBackgroundImage) {
					EchoAppRender.FillImage.render(browseBackgroundImage, overlayBrowse);
				}
				if (browseRolloverBackgroundImage) {
					this._fileElement.onmouseover = function() {EchoAppRender.FillImage.renderClear(browseRolloverBackgroundImage, overlayBrowse);};
					this._fileElement.onmouseout = function() {EchoAppRender.FillImage.renderClear(browseBackgroundImage, overlayBrowse);};
				}
			} else {
				overlayBrowse = frameDocument.createElement("button");
			}
			overlayBrowse.style[WebCore.Environment.CSS_FLOAT] = "right";
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
				if (WebCore.Environment.BROWSER_MOZILLA) {
					this._fileElement.style.marginLeft = "-32px";
				}
			}
			if (browseHeight) {
				overlayBrowse.style.height = EchoAppRender.Extent.toPixels(browseHeight, false) + "px";
			}
			EchoAppRender.Color.renderComponentProperty(this.component, "foreground", null, overlayBrowse, "color");
			EchoAppRender.Font.renderDefault(this.component, overlayBrowse);
			overlayFileDiv.appendChild(overlayBrowse);
			outerDiv.appendChild(overlayFileDiv);
			
			this._formElement.appendChild(outerDiv);
		} else {
			EchoAppRender.Color.renderComponentProperty(this.component, "foreground", null, this._fileElement, "color");
			EchoAppRender.Font.renderDefault(this.component, this._fileElement);
			this._formElement.appendChild(this._fileElement);
		}
		
		if (this.component.getRenderProperty("sendButtonDisplayed", true)) {
			this._submitElement = frameDocument.createElement("input");
			this._submitElement.type = "submit";
			this._submitElement.style.marginTop = "3px";
			var sendText = this.component.getRenderProperty("sendButtonText");
			if (sendText) {
				this._submitElement.value = sendText;
			}
			var sendWidth = this.component.getRenderProperty("sendButtonWidth");
			if (sendWidth) {
				this._submitElement.style.width = EchoAppRender.Extent.toPixels(sendWidth, true) + "px";
			}
			var sendHeight = this.component.getRenderProperty("sendButtonHeight");
			if (sendHeight) {
				this._submitElement.style.height = EchoAppRender.Extent.toPixels(sendHeight, false) + "px";
			}
			
			EchoAppRender.Font.renderDefault(this.component, this._submitElement);
			EchoAppRender.Color.renderComponentProperty(this.component, "foreground", null, this._submitElement, "color");
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
		return FileTransferRender.UploadSelectSync._receiverService + "&i=" + this.component.renderId + "&x=" + this._uploadId;
	},
	
	_createProgressUrl: function() {
		return FileTransferRender.UploadSelectSync._progressService + "&i=" + this.component.renderId + "&x=" + this._uploadId;
	},
	
	_dispose: function() {
		this._stopProgressPoller();
		if (this._submitListenerBound) {
		    WebCore.EventProcessor.remove(this._formElement, "submit", Core.method(this, this._processSubmit), false);
			this._submitListenerBound = false;
		}
		if (WebCore.Environment.BROWSER_INTERNET_EXPLORER) {
		    WebCore.EventProcessor.remove(this._frameElement, "load", Core.method(this, this._processLoad), false);
		} else {
			this._frameElement.onload = null;
		}
		if (this._loadStage == FileTransferRender.UploadSelectSync._STAGE_UPLOADING) {
		    // gracefully stop upload
		    var frameWindow = this._frameElement.contentWindow;
			if (frameWindow.stop) {
		    	frameWindow.stop();
			} else if (frameWindow.document && frameWindow.document.execCommand) {
				frameWindow.document.execCommand("Stop");
			} else {
				frameWindow.location.href = FileTransferRender.UploadSelectSync._blankWindowService;
			}
		}
		if (WebCore.Environment.BROWSER_MOZILLA) {
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