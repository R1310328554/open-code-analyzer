/* GIS OLMapWidget：OpenLayers 地图控件序列化、交互与底图行为 */
/* global QUnit, MapWidget, ol */
"use strict";

// OpenLayers MapWidget 单元测试套件
QUnit.module("gis.OLMapWidget");

// 新建 Point 控件时矢量层含默认要素，serialize 写入 textarea
QUnit.test("MapWidget.featureAdded", function (assert) {
    const options = {
        id: "id_point",
        map_id: "id_point_map",
        geom_name: "Point",
    };
    const widget = new MapWidget(options);
    assert.equal(widget.featureCollection.getLength(), 1);
    widget.serializeFeatures();
    assert.equal(
        document.getElementById("id_point").value,
        '{"type":"Point","coordinates":[7.8177,47.397]}',
        "Point added to vector layer",
    );
});

// 地图视图投影默认为 Web Mercator EPSG:3857
QUnit.test("MapWidget.map_srid", function (assert) {
    const options = {
        id: "id_point",
        map_id: "id_point_map",
        geom_name: "Point",
    };
    const widget = new MapWidget(options);
    assert.equal(
        widget.map.getView().getProjection().getCode(),
        "EPSG:3857",
        "SRID 3857",
    );
});

// defaultCenter：无坐标时为 0,0；可配置 default_lat/lon 并设缩放
QUnit.test("MapWidget.defaultCenter", function (assert) {
    const options = {
        id: "id_point",
        map_id: "id_point_map",
        geom_name: "Point",
    };
    let widget = new MapWidget(options);
    assert.equal(
        widget.defaultCenter().toString(),
        "0,0",
        "Default center at 0, 0",
    );
    options.default_lat = 47.08;
    options.default_lon = 6.81;
    widget = new MapWidget(options);
    assert.equal(
        widget.defaultCenter().toString(),
        "6.81,47.08",
        "Default center at 6.81, 47.08",
    );
    assert.equal(Math.round(widget.map.getView().getZoom()), 17);
});

// 已有几何时 draw inactive、modify active
QUnit.test("MapWidget.interactions", function (assert) {
    const options = {
        id: "id_point",
        map_id: "id_point_map",
        geom_name: "Point",
    };
    const widget = new MapWidget(options);
    assert.equal(Object.keys(widget.interactions).length, 2);
    assert.equal(
        widget.interactions.draw.getActive(),
        false,
        "Draw is inactive with an existing point",
    );
    assert.equal(
        widget.interactions.modify.getActive(),
        true,
        "Modify is active with an existing point",
    );
});

// clearFeatures 清空隐藏字段中的 GeoJSON
QUnit.test("MapWidget.clearFeatures", function (assert) {
    const options = {
        id: "id_point",
        map_id: "id_point_map",
        geom_name: "Point",
    };
    const widget = new MapWidget(options);
    const initial_value = document.getElementById("id_point").value;
    widget.clearFeatures();
    assert.equal(document.getElementById("id_point").value, "");
    document.getElementById("id_point").value = initial_value;
});

// MultiPolygon 集合类型：is_collection 为真且 draw 默认可用
QUnit.test("MapWidget.multipolygon", function (assert) {
    const options = {
        id: "id_multipolygon",
        map_id: "id_multipolygon_map",
        geom_name: "MultiPolygon",
    };
    const widget = new MapWidget(options);
    assert.ok(widget.options.is_collection);
    assert.equal(
        widget.interactions.draw.getActive(),
        true,
        "Draw is active with no existing content",
    );
});

// 各 geom_name 对应 is_collection：Multi* 与 GeometryCollection 为真
QUnit.test("MapWidget.IsCollection", function (assert) {
    const options = {
        id: "id_point",
        map_id: "id_point_map",
        geom_name: "Point",
    };
    let widget = new MapWidget(options);
    assert.notOk(widget.options.is_collection);
    // Empty the default initial Point
    document.getElementById("id_point").value = "";

    options.geom_name = "Polygon";
    widget = new MapWidget(options);
    assert.notOk(widget.options.is_collection);

    options.geom_name = "LineString";
    widget = new MapWidget(options);
    assert.notOk(widget.options.is_collection);

    options.geom_name = "MultiPoint";
    widget = new MapWidget(options);
    assert.ok(widget.options.is_collection);

    options.geom_name = "MultiPolygon";
    widget = new MapWidget(options);
    assert.ok(widget.options.is_collection);

    options.geom_name = "MultiLineString";
    widget = new MapWidget(options);
    assert.ok(widget.options.is_collection);

    options.geom_name = "GeometryCollection";
    widget = new MapWidget(options);
    assert.ok(widget.options.is_collection);
});

// layerBuilder.osm 返回 Tile 层且源为 ol.source.OSM
QUnit.test("MapWidget.layerBuilder.osm returns OSM layer", function (assert) {
    const layer = MapWidget.layerBuilder.osm();
    assert.ok(layer instanceof ol.layer.Tile, "Layer is Tile");
    assert.ok(layer.getSource() instanceof ol.source.OSM, "Source is OSM");
});

// nasaWorldview 底图使用 XYZ 源且 URL 含 earthdata.nasa.gov
QUnit.test(
    "MapWidget.layerBuilder.nasaWorldview returns XYZ layer",
    function (assert) {
        const layer = MapWidget.layerBuilder.nasaWorldview();
        assert.ok(layer instanceof ol.layer.Tile, "Layer is Tile");
        assert.ok(layer.getSource() instanceof ol.source.XYZ, "Source is XYZ");
        assert.ok(
            layer.getSource().getUrls()[0].includes("earthdata.nasa.gov"),
            "URL is NASA-hosted",
        );
    },
);

// 未指定 base_layer 时默认 OSM 底图
QUnit.test(
    "MapWidget uses default OSM base layer when none specified",
    function (assert) {
        const widget = new MapWidget({
            id: "id_point",
            map_id: "id_point_map",
            geom_name: "Point",
        });
        assert.ok(
            widget.baseLayer.getSource() instanceof ol.source.OSM,
            "Default base layer is OSM",
        );
    },
);

// base_layer 字符串从 layerBuilder 解析命名底图
QUnit.test(
    "MapWidget uses named base layer from layerBuilder",
    function (assert) {
        const widget = new MapWidget({
            id: "id_point",
            map_id: "id_point_map",
            geom_name: "Point",
            base_layer: "nasaWorldview",
        });
        assert.ok(
            widget.baseLayer.getSource() instanceof ol.source.XYZ,
            "Uses named base layer from builder",
        );
    },
);

// 传入 ol.layer 实例时直接使用，不经过 builder
QUnit.test(
    "MapWidget uses passed-in base layer object directly",
    function (assert) {
        const customLayer = new ol.layer.Tile({ source: new ol.source.OSM() });
        const widget = new MapWidget({
            id: "id_point",
            map_id: "id_point_map",
            geom_name: "Point",
            base_layer: customLayer,
        });
        assert.strictEqual(
            widget.baseLayer,
            customLayer,
            "Uses provided layer object",
        );
    },
);

// initMapWidgetInSection 扫描 dj_map_wrapper，跳过 formset __prefix__ 模板
QUnit.test(
    "initMapWidgetInSection initializes widgets and skips __prefix__",
    function (assert) {
        const wrapper1 = document.createElement("div");
        wrapper1.className = "dj_map_wrapper";
        wrapper1.id = "id_point_map_wrapper";
        wrapper1.innerHTML = `
        <textarea id="id_point"></textarea>
        <div class="dj_map" id="id_point_map"></div>
        <script type="application/json" id="id_point_mapwidget_options">
            { "geom_name": "Point" }
        </script>
    `;
        document.body.appendChild(wrapper1);

        const wrapper2 = document.createElement("div");
        wrapper2.className = "dj_map_wrapper";
        wrapper2.id = "form-__prefix__-map_wrapper";
        wrapper2.innerHTML = `
        <textarea id="id_fake"></textarea>
        <div class="dj_map" id="id_fake_map"></div>
        <script type="application/json" id="id_fake_mapwidget_options">
            { "geom_name": "MultiPoint" }
        </script>
    `;

        document.body.appendChild(wrapper2);

        const maps = window.initMapWidgetInSection(document);

        assert.equal(maps.length, 1, "Only one map widget is initialized");
        assert.ok(maps[0] instanceof MapWidget, "Map is instance of MapWidget");
        assert.equal(
            maps[0].options.id,
            "id_point",
            "Correct widget was initialized",
        );
        assert.equal(
            maps[0].options.map_id,
            "id_point_map",
            "Map ID is correct",
        );

        // Clean up
        wrapper1.remove();
        wrapper2.remove();
    },
);
