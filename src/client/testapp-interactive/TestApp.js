init = function() {
    Core.Debug.consoleElement = document.getElementById("debugconsole");
    Core.Web.init();

    var app = new TestApp();
    var client = new Echo.FreeClient(app, document.getElementById("rootArea"));
    client.loadStyleSheet("Default.stylesheet.xml");
    client.init();
};

TestApp = Core.extend(Echo.Application, {

    $static: {
        Tests: { },

        randomColor: function() {
            var colorValue = parseInt(Math.random() * 0x1000000).toString(16);
            colorValue = "#" + "000000".substring(colorValue.length) + colorValue;
            return colorValue;
        }
    },

    $construct: function() {
        Echo.Application.call(this);
        var testScreen = new TestApp.TestScreen();
        testScreen.addTest("UploadSelect");
        testScreen.addTest("MultipleUploadSelect");
        this.rootComponent.add(testScreen);
    }
});

TestApp.TestScreen = Core.extend(Echo.ContentPane, {

    $construct: function() {
        Echo.ContentPane.call(this, {
            background: "#abcdef",
            children: [
                this.testSelectSplitPane = new Echo.SplitPane({
                    styleName: "DefaultResizable",
                    separatorPosition: 180,
                    children: [
                        this.testSelectColumn = new Echo.Column({
                            insets: "5px 10px"
                        }),
                        new Echo.Column({
                            insets: "5px 10px",
                            children: [
                                new Echo.Label({
                                    styleName: "Default",
                                    text: "Welcome to the Experimental Echo Filetransfer Test Application!"
                                })
                            ]
                        })
                    ]
                })
            ]
        });
    },
    
    addTest: function(testName) {
        this.testSelectColumn.add(new Echo.Button({
            styleName: "Default",
            text: testName,
            events: {
                action: Core.method(this, this._launchTest)
            }
        }));
    },

    _launchTest: function(e) {
        while (this.testSelectSplitPane.children.length > 1) {
            this.testSelectSplitPane.remove(1);
        }
        var testName = e.source.get("text");
        var test = TestApp.Tests[testName];
        if (!test) {
            alert("Test not found: " + testName);
            return;
        }
        var instance = new test();
        this.testSelectSplitPane.add(instance);
    }
});

TestApp.TestPane = Core.extend(Echo.ContentPane, {

    $construct: function() {
        Echo.ContentPane.call(this, {
            children: [
                new Echo.SplitPane({
                    styleName: "DefaultResizable",
                    orientation: Echo.SplitPane.ORIENTATION_HORIZONTAL_LEADING_TRAILING,
                    separatorPosition: 180,
                    children: [
                        this.controlsColumn = new Echo.Column({
                            insets: "5px 10px"
                        }),
                        this.content = new Echo.ContentPane()
                    ]
                })
            ]
        });
    },

    addTestButton: function(text, action) {
        this.controlsColumn.add(
            new Echo.Button({
                styleName: "Default",
                text: text,
                events: {
                    action: action 
                }
            })
        );
    }
});

TestApp.Tests.UploadSelect = Core.extend(TestApp.TestPane, {

    $construct: function() {
        TestApp.TestPane.call(this);

        this.childCount = 0;
        this.uploadSelect = new FileTransfer.UploadSelect({receiver : "http://foo.bar/uploadeURL?x=1"});

        var regularButton = new Echo.Button({
            text : "A Button",
            styleName: "StyledButton"
        });

        var column = new Echo.Column({
             children : [ this.uploadSelect, regularButton]
        });

        this.content.add(column);

        this.addTestButton("Set no border", Core.method(this, this._setBorderNone));
        this.addTestButton("Set 3px blue border", Core.method(this, this._setBorder3pxBlue));
        this.addTestButton("Insets=null", Core.method(this, this._insetsNull));
        this.addTestButton("Insets=10px", Core.method(this, this._insets10));
        this.addTestButton("Random Background", Core.method(this, this._setRandomBackground));
        this.addTestButton("Apply Test Stylesheet", Core.method(this, this._applyStyle));
    },

    _insetsNull: function() {
        this.uploadSelect.set("insets", null);
    },

    _insets10: function() {
        this.uploadSelect.set("insets", 10);
    },

    _setRandomBackground: function() {
        this.uploadSelect.set("background", TestApp.randomColor());
    },

    _setBorderNone: function() {
        this.uploadSelect.set("border", null);
    },

    _setBorder3pxBlue: function() {
        this.uploadSelect.set("border", "3px solid #0000ff");
    },

    _applyStyle: function() {
        this.uploadSelect.setStyleName("StyledUpload");
    }

});


TestApp.Tests.MultipleUploadSelect = Core.extend(TestApp.TestPane, {

    $construct: function() {
        TestApp.TestPane.call(this);

        this.childCount = 0;

        this.uploadSelect = new FileTransfer.MultipleUploadSelect({
            receiver : "http://foo.bar/uploadeURL?x=1",
            flashUrl : "../swfupload.swf"
        });

        var regularButton = new Echo.Button({
            text : "A Button",
            styleName: "StyledButton"
        });

        var column = new Echo.Column({
            children : [ this.uploadSelect, regularButton]
        });

        this.content.add(column);

        this.addTestButton("Set no border", Core.method(this, this._setBorderNone));
        this.addTestButton("Set 3px blue border", Core.method(this, this._setBorder3pxBlue));
        this.addTestButton("Insets=null", Core.method(this, this._insetsNull));
        this.addTestButton("Insets=10px", Core.method(this, this._insets10));
        this.addTestButton("Random Background", Core.method(this, this._setRandomBackground));
        this.addTestButton("Random Foreground", Core.method(this, this._setRandomForeground));
        this.addTestButton("Apply Test Stylesheet", Core.method(this, this._applyStyle));
    },

    _insetsNull: function() {
        this.uploadSelect.set("insets", null);
    },

    _insets10: function() {
        this.uploadSelect.set("insets", 10);
    },

    _setRandomBackground: function() {
        this.uploadSelect.set("background", TestApp.randomColor());
    },

    _setRandomForeground: function() {
        this.uploadSelect.set("foreground", TestApp.randomColor());
    },

    _setBorderNone: function() {
        this.uploadSelect.set("border", null);
    },

    _setBorder3pxBlue: function() {
        this.uploadSelect.set("border", "3px solid #0000ff");
    },

    _applyStyle: function() {
        this.uploadSelect.setStyleName("StyledUpload");
    }

});

