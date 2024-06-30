package org.openstreetmap.gui.jmapviewer.tilesources.astro;

import org.openstreetmap.gui.jmapviewer.tilesources.TemplatedTMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSourceInfo;

public class OPMMarsShadedSurfaceTileSource extends TemplatedTMSTileSource {

    /**
     * Creates Templated TMS Tile Source based on ImageryInfo
     *
     */
    public OPMMarsShadedSurfaceTileSource() {
        super(new TileSourceInfo(
        "Mars Shaded Surface Texture",
        "http://s3-eu-west-1.amazonaws.com/whereonmars.cartodb.net/celestia_mars-shaded-16k_global/{z}/{x}/{y}.png",
            "opm-mars-shaded"
        ));
    }
}
