package io.github.xiaocihua.stacktonearbychests.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsGui;
import io.github.xiaocihua.stacktonearbychests.gui.ModOptionsScreen;

public class ModMenuApiImpl implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ModOptionsScreen(new ModOptionsGui()) {
            @Override
            public void close() {
                client.setScreen(parent);
            }
        };
    }
}