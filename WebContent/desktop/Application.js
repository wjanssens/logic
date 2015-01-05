Ext.Loader.setConfig({"enabled": true, "disableCaching": false});

Ext.state.Manager.setProvider(new Ext.state.LocalStorageProvider());

Ext.application({
	"name": "Scheduler",
	"appFolder": "desktop",
	"views": [
		"Viewport"
	],
	"controllers": [
		"Node"
	],
	"launch": function() {
		Scheduler.app = this;
		Ext.create( "Scheduler.view.Viewport");
		
		Ext.Date.patterns = {
			"Date": "Y-m-d",
			"DateTime": "Y-m-d H:i",
			"Time": "H:i"
		};
	},
	"failure": function(response) {
		var message;
		var title;
		if (Ext.isString(response)) {
			message = response;
			title = "Error";
		} else if (response.responseText != null) {
			title = (response.statusText ? response.statusText : "Error");
			var json = Ext.decode(response.responseText, true);
			if (json == null){
				message = response.responseText;
			}
			else {
				message = json.msg;
			}
		} else {
			message = "Unknown error";
			title = "Error";
		}
		
		Ext.MessageBox.show({
			"title": title,
			"msg": message,
			"buttons": Ext.Msg.OK
		});
	}
});

Ext.override('Ext.form.field.DateField', {
	"format": "Y-m-d",
});

Ext.override(Ext.data.proxy.Ajax, { "timeout": 60000 });

