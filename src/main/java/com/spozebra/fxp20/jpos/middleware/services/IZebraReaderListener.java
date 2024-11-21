package com.spozebra.fxp20.jpos.middleware.services;

import java.util.List;

public interface IZebraReaderListener {
    void onTagsRead(List<String> readTags);
}
