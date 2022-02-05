package com.mcmoddev.mmdbot.utilities.scripting.object;

import com.mcmoddev.mmdbot.utilities.scripting.ExposeScripting;
import net.dv8tion.jda.api.entities.RoleIcon;

public class ScriptRoleIcon {

    @ExposeScripting
    public final String id;
    @ExposeScripting
    public final String url;
    @ExposeScripting
    public final String emoji;

    public ScriptRoleIcon(RoleIcon roleIcon) {
        this.id = roleIcon.getIconId();
        this.url = roleIcon.getIconUrl();
        this.emoji = roleIcon.getEmoji();
    }

}
