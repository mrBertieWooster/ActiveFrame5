package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
@Root(name = "themes")
public class ThemesConfig implements Dto {
    @ElementList(inline = true, name ="theme", required = true)
    List<ThemeConfig> themes;

    public List<ThemeConfig> getThemes() {
        return themes;
    }

    public void setThemes(List<ThemeConfig> themes) {
        this.themes = themes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ThemesConfig)) {
            return false;
        }

        ThemesConfig that = (ThemesConfig) o;

        if (themes != null ? !themes.equals(that.themes) : that.themes != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return themes != null ? themes.hashCode() : 0;
    }
}
