package ru.intertrust.cm.core.gui.model.plugin;

/**
 * @author Sergey.Okolot
 */
public class DomainObjectSurferPluginState implements PluginState {
    private boolean toggleEdit;

    public boolean isToggleEdit() {
        return toggleEdit;
    }

    public void setToggleEdit(boolean toggleEdit) {
        this.toggleEdit = toggleEdit;
    }

    @Override
    public DomainObjectSurferPluginState createClone() {
        final DomainObjectSurferPluginState clone = new DomainObjectSurferPluginState();
        clone.toggleEdit = this.toggleEdit;
        return clone;
    }
}
