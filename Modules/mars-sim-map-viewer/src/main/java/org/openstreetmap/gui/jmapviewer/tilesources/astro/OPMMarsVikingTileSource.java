package org.openstreetmap.gui.jmapviewer.tilesources.astro;

import org.openstreetmap.gui.jmapviewer.tilesources.TemplatedTMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSourceInfo;

public class OPMMarsVikingTileSource extends TemplatedTMSTileSource {

    /**
     * Creates Templated TMS Tile Source based on ImageryInfo
     *
     */
    public OPMMarsVikingTileSource() {
        super(new TileSourceInfo(
        "Mars Viking MDIM2.1",
        "http://s3-eu-west-1.amazonaws.com/whereonmars.cartodb.net/viking_mdim21_global/{z}/{x}/{y}.png",
            "opm-bars-viking"
        ));
    }
}
