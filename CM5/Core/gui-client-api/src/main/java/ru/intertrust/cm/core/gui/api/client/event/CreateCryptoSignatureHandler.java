package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.EventHandler;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.crypto.SignedResult;

public interface CreateCryptoSignatureHandler extends EventHandler{

    void onCreateCryptoSignature(SignedResult signedResult);

    void onErrorCreateCryptoSignature(Id rootId, String message);
}
