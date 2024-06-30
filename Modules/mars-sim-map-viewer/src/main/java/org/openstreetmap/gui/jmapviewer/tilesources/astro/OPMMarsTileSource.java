package org.openstreetmap.gui.jmapviewer.tilesources.astro;

import org.openstreetmap.gui.jmapviewer.Tile;
import org.openstreetmap.gui.jmapviewer.TileRange;
import org.openstreetmap.gui.jmapviewer.TileXY;
import org.openstreetmap.gui.jmapviewer.tilesources.AbstractOsmTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.AbstractTMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TemplatedTMSTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.TileSourceInfo;

public class OPMMarsTileSource extends TemplatedTMSTileSource {

    /**
     * Creates Templated TMS Tile Source based on ImageryInfo
     *
     */
    public OPMMarsTileSource() {
        super(new TileSourceInfo(
        "Mars Base Map",
        "https://cartocdn-gusc.global.ssl.fastly.net/opmbuilder/api/v1/map/named/opm-mars-basemap-v0-2/all/{z}/{x}/{y}.png",
            "opm-bars-base"
        ));
    }
}
