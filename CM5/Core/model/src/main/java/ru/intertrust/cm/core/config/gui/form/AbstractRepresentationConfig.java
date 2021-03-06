package ru.intertrust.cm.core.config.gui.form;

import org.simpleframework.xml.Element;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.FormattingConfig;
import ru.intertrust.cm.core.config.gui.form.widget.PatternConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.10.2014
 *         Time: 17:59
 */
public abstract class AbstractRepresentationConfig implements Dto {
    @Element(name = "pattern", required = false)
    private PatternConfig patternConfig;

    @Element(name = "formatting", required = false)
    private FormattingConfig formattingConfig;

    public PatternConfig getPatternConfig() {
        return patternConfig;
    }

    public void setPatternConfig(PatternConfig patternConfig) {
        this.patternConfig = patternConfig;
    }

    public FormattingConfig getFormattingConfig() {
        return formattingConfig;
    }

    public void setFormattingConfig(FormattingConfig formattingConfig) {
        this.formattingConfig = formattingConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AbstractRepresentationConfig that = (AbstractRepresentationConfig) o;

        if (formattingConfig != null ? !formattingConfig.equals(that.formattingConfig) : that.formattingConfig != null) {
            return false;
        }
        if (patternConfig != null ? !patternConfig.equals(that.patternConfig) : that.patternConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = patternConfig != null ? patternConfig.hashCode() : 0;
        result = 31 * result + (formattingConfig != null ? formattingConfig.hashCode() : 0);
        return result;
    }
}
