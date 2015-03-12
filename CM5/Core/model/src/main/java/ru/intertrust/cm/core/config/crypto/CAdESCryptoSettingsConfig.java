package ru.intertrust.cm.core.config.crypto;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

import ru.intertrust.cm.core.config.CollectorSettings;

@Root(name = "cades-crypto-settings-config")
public class CAdESCryptoSettingsConfig implements CollectorSettings{
    private static final long serialVersionUID = -6823371274654533928L;
    
    @Attribute(name = "ts-address", required=true)
    private String tsAddress;
    
    public String getTsAddress() {
        return tsAddress;
    }

    public void setTsAddress(String tsAddress) {
        this.tsAddress = tsAddress;
    }    

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tsAddress == null) ? 0 : tsAddress.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CAdESCryptoSettingsConfig other = (CAdESCryptoSettingsConfig) obj;
        if (tsAddress == null) {
            if (other.tsAddress != null)
                return false;
        } else if (!tsAddress.equals(other.tsAddress))
            return false;
        return true;
    }
    
    
}