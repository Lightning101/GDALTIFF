/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gdaltiff;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;
import org.gdal.gdalconst.gdalconstConstants;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;

/**
 *
 * @author dhinendra.rajapakse
 */
public class GDALTiff
{

    public GDALTiff(String filename)
    {

    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {   
        createGeoTiffFromGeoJson(new File("G:\\test.geojson"),new File("G:\\test.tif"),1000,1000,new Color(255, 1, 1, 255),0);
    }

    /**
     * 
     * @param geoJson GeoJson file to read from.
     * @param output Output file to create tiff.
     * @param resX The x resolution of new image
     * @param resY The x resolution of new image
     * @param burnColor The color which the geojson features will be colored in.
     * @param noDataValue The nodata value for geotiff image.
     */
    public static void createGeoTiffFromGeoJson(File geoJson, File output, int resX, int resY, Color burnColor, int noDataValue)
    {
        FileReader fileReader = null;
        try
        {
            gdal.AllRegister();
            Driver driver = gdal.GetDriverByName("GTiff");

            driver.Register();

            fileReader = new FileReader(geoJson);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder strBuild = new StringBuilder("");
            bufferedReader.lines().forEach(str ->
            {
                strBuild.append(str);
            });

            DataSource dataSource = ogr.Open(strBuild.toString());
            Layer layer = dataSource.GetLayer(0);
            System.out.println(dataSource.GetLayerCount());
            final Vector<String> imageCreationOptions = new Vector<>(1);
            imageCreationOptions.add("COMPRESS=LZW");
            Dataset newDateSet = driver.Create(output.getAbsolutePath(), resX, resY, 4, gdalconst.GDT_Byte, imageCreationOptions);

            double[] extent = layer.GetExtent();

            double x_res = ((extent[1] - extent[0]) / resX);
            double y_res = ((extent[3] - extent[2]) / resY);

            newDateSet.SetGeoTransform(new double[]
            {
                extent[0], x_res, 0, extent[3], 0, -y_res
            });
            
            
            // TODO figure out a proper way to get a projection from datasource 
            newDateSet.SetProjection("GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\",SPHEROID[\"WGS 84\",6378137,298.257223563,AUTHORITY[\"EPSG\",\"7030\"]],AUTHORITY[\"EPSG\",\"6326\"]],PRIMEM[\"Greenwich\",0],UNIT[\"degree\",0.0174532925199433],AUTHORITY[\"EPSG\",\"4326\"]]");

            newDateSet.GetRasterBand(1).SetNoDataValue(noDataValue);
            newDateSet.GetRasterBand(2).SetNoDataValue(noDataValue);
            newDateSet.GetRasterBand(3).SetNoDataValue(noDataValue);
            newDateSet.GetRasterBand(4).SetNoDataValue(noDataValue);

            newDateSet.GetRasterBand(1).FlushCache();
            newDateSet.GetRasterBand(2).FlushCache();
            newDateSet.GetRasterBand(3).FlushCache();
            newDateSet.GetRasterBand(4).FlushCache();

            int[] band =
            {
                1, 2, 3, 4
            };
            double[] burn =
            {
                burnColor.getRed(), burnColor.getGreen(), burnColor.getBlue(), burnColor.getAlpha()
            };

            gdal.RasterizeLayer(newDateSet, band, layer, burn);
            gdal.GDALDestroyDriverManager();
        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(GDALTiff.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally
        {
            try
            {
                fileReader.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(GDALTiff.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
