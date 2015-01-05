Ext.define("Scheduler.view.Viewport", {
	"extend": "Ext.container.Viewport", 
	
	"layout": "border",

	"items": [
		{
			"itemId": "plans",
			"region": "west",
			"width": 256,
			"xtype": "grid",
			"hideHeaders": true,
			"fields": [ "id", "name" ],
			"columns": [ { "name": "name", "flex": 1 } ],
			"proxy": {
				"type": "ajax",
				"url": "plans.json",
				"reader": {
					"type": "json"
				}
			},
			"tools": [
				{
					"type": "plus"
				}
			]
		},
		{
			"itemId": "plan",
			"region": "center",
			"xtype": "tabpanel",
			"title": "Rooms",
			"items": [
				{
					"itemId": "blocks",
					"xtype": "grid",
					"fields": [ "id", "name" ],
					"columns": [ { "name": "name" }],
					"proxy": {
						"type": "ajax",
						"reader": {
							"type": "json"
						}
					},
					"tools": [
						{
							"type": "plus"
						}
					]
				},
				{
					"itemId": "rooms",
					"xtype": "grid",
					"fields": [ "id", "name" ],
					"columns": [ { "name": "name" }],
					"proxy": {
						"type": "ajax",
						"reader": {
							"type": "json"
						}
					},
					"tools": [
						{
							"type": "plus"
						}
					]
				},
				{
					"itemId": "classes",
					"xtype": "grid",
					"fields": [ "id", "name" ],
					"columns": [ { "name": "name" }],
					"proxy": {
						"type": "ajax",
						"reader": {
							"type": "json"
						}
					},
					"tools": [
						{
							"type": "plus"
						}
					]
				},
				{
					"itemId": "teachers",
					"xtype": "grid",
					"fields": [ "id", "name" ],
					"columns": [ { "name": "name" }],
					"proxy": {
						"type": "ajax",
						"reader": {
							"type": "json"
						}
					},
					"tools": [
						{
							"type": "plus"
						}
					]
				}
			]
		}
	]
});