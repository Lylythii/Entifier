package wiresegal.thicc.asm;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author WireSegal
 * Created at 6:14 PM on 4/10/18.
 */
@IFMLLoadingPlugin.Name("ThiccEntities Plugin")
@IFMLLoadingPlugin.TransformerExclusions("wiresegal.thicc.asm")
@IFMLLoadingPlugin.SortingIndex(1001)
public class ThiccCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
                "wiresegal.thicc.asm.ThiccAsmTransformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // NO-OP
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
