package com.mcmoddev.mmdbot.dashboard.client.controller.config;

import com.mcmoddev.mmdbot.dashboard.BotTypeEnum;
import com.mcmoddev.mmdbot.dashboard.client.DashboardClient;
import com.mcmoddev.mmdbot.dashboard.client.util.Constants;
import com.mcmoddev.mmdbot.dashboard.common.packet.PacketID;
import com.mcmoddev.mmdbot.dashboard.packets.UpdateConfigPacket;
import com.mcmoddev.mmdbot.dashboard.packets.requests.RequestConfigValuePacket;
import com.mcmoddev.mmdbot.dashboard.util.DashConfigType;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.text.Font;

public class DefaultConfigBoxController extends ConfigBoxController {

    public DefaultConfigBoxController(final BotTypeEnum botType, final DashConfigType type, final String configName, final String path, final String[] comments) {
        super(botType, type, configName, path, comments);
    }

    @FXML
    private Label configNameLabel;
    @FXML
    private Label commentsLabel;
    @FXML
    private TextField valueField;

    @Override
    public void init() {
        configNameLabel.setText(configName);
        commentsLabel.setText(Constants.LINE_JOINER.join(comments));
        configNameLabel.setFont(Font.font(20));
        DashboardClient.sendAndAwaitResponseWithID((PacketID id) ->
                new RequestConfigValuePacket(id, botType, type, configName, path))
            .withPlatformAction(pkt -> valueField.setText(pkt.value().toString()))
            .queue();
    }

    @FXML
    public void onUpdateValueClick(Event e) {
        DashboardClient.sendAndAwaitGenericResponse(id -> new UpdateConfigPacket(id, botType, type, configName, path, valueField.getText()))
            .queue();
    }

}
