package wiresegal.thicc.client;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wiresegal.thicc.ThiccEntities;

import java.util.List;
import java.util.Set;

import static wiresegal.thicc.ThiccEntities.THICC;

/**
 * @author WireSegal
 * Created at 10:44 PM on 4/10/18.
 */
@SideOnly(Side.CLIENT)
public class ThiccGooeyHandler implements IModGuiFactory {
    @Override
    public void initialize(Minecraft minecraftInstance) {
        // NO-OP
    }

    @Override
    public boolean hasConfigGui() {
        return true;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new ThiccGooey(parentScreen);
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    public static class ThiccGooey extends GuiConfig {
        public ThiccGooey(GuiScreen parentScreen) {
            super(parentScreen, getConfigElements(), THICC, true, false, "T H I C C");
        }

        private static List<IConfigElement> getConfigElements() {
            List<IConfigElement> elements = Lists.newArrayList();

            Configuration configuration = ThiccEntities.config.configInstance;
            for (String category : configuration.getCategoryNames()) {
                ConfigCategory ct = configuration.getCategory(category);
                elements.add(new ConfigElement(ct));
            }

            return elements;
        }

    }
}
