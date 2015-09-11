package cz.crcs.sekan.miip;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    private static PageOrientation pageOrientation = PageOrientation.LANDSCAPE;
    private static PDRectangle pageSize = PDPage.PAGE_SIZE_A4;
    private static Border border = new Border(20);
    private static float spaceBetween = 0;
    private static boolean renderBorders = false;
    private static int rows = 2;
    private static int cols = 3;
    private static String outputFileName = "miip.pdf";

    public static void main(String[] args) {
        String[] fileNames = ParseArgs(args);
        if (fileNames.length == 0) {
            System.out.print("MergeImagesIntoPDF beta\n" +
                    "Author: Peter Sekan, uco 433390, peter.sekan@mail.muni.cz\n" +
                    "Usage: citp [-orientation:portrait|landscape]\n" +
                    "            [-size:a0|a1|a2|a3|a4|a5|a6]\n" +
                    "            [-border:size|w,h|top,right,bottom,left]\n" +
                    "            [-sb:int]\n" +
                    "            [-rb:0|1]\n" +
                    "            [-rows:int]\n" +
                    "            [-cols:int]\n" +
                    "            [-o:string]\n" +
                    "            file1 file2 file3 ...\n" +
                    "\n" +
                    "Description:" +
                    " - Parameter sb is border between images.\n" +
                    " - Parameter rb is option for render border around images.\n" +
                    " - Parameter o is output file name for pdf without space character.\n" +
                    " - Parameters border and bb is in points (unit of measure).\n" +
                    " - Default values for parameters:\n" +
                    "   > orientation = landscape\n" +
                    "   > size        = a4\n" +
                    "   > border      = 20\n" +
                    "   > sb          = 0\n" +
                    "   > rb          = 0\n" +
                    "   > rows        = 2\n" +
                    "   > cols        = 3\n" +
                    "   > o           = miip.pdf\n");
            return;
        }

        if (pageOrientation == PageOrientation.LANDSCAPE) {
            pageSize = new PDRectangle(pageSize.getHeight(), pageSize.getWidth());
        }

        PDDocument document = new PDDocument();
        PDPage page = null;
        PDPageContentStream contentStream = null;
        for (int fileIndex = 0; fileIndex < fileNames.length; fileIndex++) {
            int fileIndexOnPage = fileIndex % (rows * cols);
            if (fileIndexOnPage == 0 || page == null) {
                try {
                    if (contentStream != null) contentStream.close();
                    page = new PDPage(pageSize);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                } catch (IOException ex) {
                    System.err.println("Cannot create or close contentStream: " + ex);
                }
            }

            int actualRow = fileIndexOnPage / cols;
            int actualCol = fileIndexOnPage % cols;

            try {
                BufferedImage awtImage = ImageIO.read(new File(fileNames[fileIndex]));
                PDPixelMap image = new PDPixelMap(document, awtImage);
                RenderPosition boxPosition = computeBoxPosition(actualRow, actualCol);
                RenderPosition imagePosition = computeImagePosition(boxPosition, image.getWidth(), image.getHeight());
                contentStream.drawXObject(image, imagePosition.getX(), imagePosition.getY(), imagePosition.getWidth(), imagePosition.getHeight());

                if (renderBorders) {
                    contentStream.drawLine(boxPosition.getX(), boxPosition.getY(), boxPosition.getX() + boxPosition.getWidth(), boxPosition.getY());
                    contentStream.drawLine(boxPosition.getX(), boxPosition.getY() + boxPosition.getHeight(), boxPosition.getX() + boxPosition.getWidth(), boxPosition.getY() + boxPosition.getHeight());
                    contentStream.drawLine(boxPosition.getX(), boxPosition.getY(), boxPosition.getX(), boxPosition.getY() + boxPosition.getHeight());
                    contentStream.drawLine(boxPosition.getX() + boxPosition.getWidth(), boxPosition.getY(), boxPosition.getX() + boxPosition.getWidth(), boxPosition.getY() + boxPosition.getHeight());
                }
            } catch (NullPointerException | IOException ex) {
                System.err.println("Cannot read image '" + fileNames[fileIndex] + "': " + ex);
            }
            System.out.println("Image '" + fileNames[fileIndex] + "' rendered.");
        }

        try {
            if (contentStream != null)
                contentStream.close();
            document.save(outputFileName);
            document.close();
        } catch (IOException ex) {
            System.err.println("Cannot close PDF file: " + ex);
        } catch (COSVisitorException ex) {
            System.err.println("Cannot save or close PDF file: " + ex);
        }
    }

    private static RenderPosition computeImagePosition(RenderPosition boxPosition, float imageWidth, float imageHeight) {
        float image_x, image_y, image_w, image_h;
        float temp_height = imageHeight / (imageWidth / boxPosition.getWidth());
        if (temp_height > boxPosition.getHeight()) {
            image_h = boxPosition.getHeight();
            image_y = boxPosition.getY();
            image_w = imageWidth / (imageHeight / boxPosition.getHeight());
            image_x = boxPosition.getX() + (boxPosition.getWidth() - image_w) / 2;
        } else {
            image_w = boxPosition.getWidth();
            image_x = boxPosition.getX();
            image_h = temp_height;
            image_y = boxPosition.getY() + (boxPosition.getHeight() - image_h) / 2;
        }

        return new RenderPosition(image_x, image_y, image_w, image_h);
    }

    private static RenderPosition computeBoxPosition(int row, int col) {
        float box_w = (pageSize.getWidth() - (border.getLeft() + border.getRight() + (cols - 1) * spaceBetween)) / cols;
        float box_h = (pageSize.getHeight() - (border.getTop() + border.getBottom() + (rows - 1) * spaceBetween)) / rows;
        float box_x = border.getLeft() + col * (spaceBetween + box_w);
        float box_y = border.getBottom() + (rows - row - 1) * (spaceBetween + box_h);
        return new RenderPosition(box_x, box_y, box_w, box_h);
    }

    private static String[] ParseArgs(String[] args) {
        ArrayList<String> fileNames = new ArrayList<>();

        for (String argument : args) {
            if (argument.charAt(0) != '-')
                fileNames.add(argument);
            else {
                int pos = argument.indexOf(':');
                if (pos == -1) {
                    System.err.println("Wrong parameter '" + argument + "'.");
                    continue;
                }
                String parameter = argument.substring(1, pos);
                String value = argument.substring(pos + 1);
                switch (parameter.toLowerCase()) {
                    case "orientation":
                        switch (value.toLowerCase()) {
                            case "portrait":
                                pageOrientation = PageOrientation.PORTRAIT;
                                break;
                            case "landscape":
                                pageOrientation = PageOrientation.LANDSCAPE;
                                break;
                            default:
                                System.err.println("Wrong value for parameter " + parameter + ".");
                        }
                        break;
                    case "size":
                        switch (value.toLowerCase()) {
                            case "a0":
                                pageSize = PDPage.PAGE_SIZE_A0;
                                break;
                            case "a1":
                                pageSize = PDPage.PAGE_SIZE_A1;
                                break;
                            case "a2":
                                pageSize = PDPage.PAGE_SIZE_A2;
                                break;
                            case "a3":
                                pageSize = PDPage.PAGE_SIZE_A3;
                                break;
                            case "a4":
                                pageSize = PDPage.PAGE_SIZE_A4;
                                break;
                            case "a5":
                                pageSize = PDPage.PAGE_SIZE_A5;
                                break;
                            case "a6":
                                pageSize = PDPage.PAGE_SIZE_A6;
                                break;
                            default:
                                System.err.println("Wrong value for parameter " + parameter + ".");
                        }
                        break;
                    case "border":
                        String[] borders = value.split(",");
                        try {
                            switch (borders.length) {
                                case 1:
                                    border = new Border(Float.parseFloat(borders[0]));
                                    break;
                                case 2:
                                    border = new Border(Float.parseFloat(borders[0]), Float.parseFloat(borders[1]));
                                    break;
                                case 4:
                                    border = new Border(Float.parseFloat(borders[0]),
                                            Float.parseFloat(borders[1]),
                                            Float.parseFloat(borders[2]),
                                            Float.parseFloat(borders[3]));
                                    break;
                                default:
                                    System.err.println("Wrong value for parameter " + parameter + ".");
                            }
                        } catch (NumberFormatException ex) {
                            System.err.println("Wrong value for parameter " + parameter + ".");
                        }
                        break;
                    case "sb":
                        try {
                            float temp = Float.parseFloat(value);
                            if (temp < 0)
                                System.err.println("Wrong value for parameter " + parameter + ".");
                            else
                                spaceBetween = temp;
                        } catch (NumberFormatException ex) {
                            System.err.println("Wrong value for parameter " + parameter + ".");
                        }
                        break;
                    case "rb":
                        switch (value.toLowerCase()) {
                            case "0":
                                renderBorders = false;
                                break;
                            case "1":
                                renderBorders = true;
                                break;
                            default:
                                System.err.println("Wrong value for parameter " + parameter + ".");
                        }
                        break;
                    case "rows":
                        try {
                            int temp = Integer.parseInt(value);
                            if (temp < 1)
                                System.err.println("Wrong value for parameter " + parameter + ".");
                            else
                                rows = temp;
                        } catch (NumberFormatException ex) {
                            System.err.println("Wrong value for parameter " + parameter + ".");
                        }
                        break;
                    case "cols":
                        try {
                            int temp = Integer.parseInt(value);
                            if (temp < 1)
                                System.err.println("Wrong value for parameter " + parameter + ".");
                            else
                                cols = temp;
                        } catch (NumberFormatException ex) {
                            System.err.println("Wrong value for parameter " + parameter + ".");
                        }
                        break;
                    case "o":
                        if (value.length() == 0)
                            System.err.println("Wrong value for parameter " + parameter + ".");
                        else
                            outputFileName = value;
                        break;
                    default:
                        System.err.println("Wrong parameter " + parameter + ".");
                }
            }
        }

        return fileNames.toArray(new String[fileNames.size()]);
    }
}
