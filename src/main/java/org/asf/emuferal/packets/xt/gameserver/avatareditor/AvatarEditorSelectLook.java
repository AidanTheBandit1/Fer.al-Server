package org.asf.emuferal.packets.xt.gameserver.avatareditor;

import java.io.IOException;

import org.asf.emuferal.data.XtReader;
import org.asf.emuferal.data.XtWriter;
import org.asf.emuferal.networking.smartfox.SmartfoxClient;
import org.asf.emuferal.packets.xt.IXtPacket;
import org.asf.emuferal.players.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AvatarEditorSelectLook implements IXtPacket<AvatarEditorSelectLook> {

	private String lookID;

	@Override
	public AvatarEditorSelectLook instantiate() {
		return new AvatarEditorSelectLook();
	}

	@Override
	public String id() {
		return "als";
	}

	@Override
	public void parse(XtReader reader) throws IOException {
		lookID = reader.read();
	}

	@Override
	public void build(XtWriter writer) throws IOException {
		writer.writeInt(-1); // Data prefix

		writer.writeString(lookID);

		writer.writeString(""); // Data suffix
	}

	@Override
	public boolean handle(SmartfoxClient client) throws IOException {
		// Switch looks
		Player plr = (Player) client.container;

		// Save the pending look ID
		plr.pendingLookID = lookID;
		plr.activeLook = plr.pendingLookID;

		// Respond with switch packet
		plr.client.sendPacket(this);

		// Save active look
		plr.account.setActiveLook(plr.activeLook);

		// Assign the defID
		JsonArray items = plr.account.getPlayerInventory().getItem("avatars").getAsJsonArray();
		JsonObject lookObj = null;
		for (JsonElement itm : items) {
			if (itm.isJsonObject()) {
				JsonObject obj = itm.getAsJsonObject();
				if (obj.get("id").getAsString().equals(plr.activeLook)) {
					lookObj = obj;
					break;
				}
			}
		}

		plr.pendingLookDefID = 8254;
		if (lookObj != null) {
			plr.pendingLookDefID = lookObj.get("defId").getAsInt();
		}

		return true;
	}

}
