# MIIP
**MergeImagesIntoPdf beta**
Create one PDF file contains images (graphs etc.)

_Compile with java language level 7._

#Usage
```
miip [-orientation:portrait|landscape]
     [-size:a0|a1|a2|a3|a4|a5|a6]
     [-border:size|w,h|top,right,bottom,left]
     [-sb:int]
     [-rb:0|1]
     [-rows:int]
     [-cols:int]
     [-o:string]
     file1 file2 file3 ...
```

#Parameters description
 - Parameter sb is border between images.
 - Parameter rb is option for render border around images.
 - Parameter o is output file name for pdf without space character.
 - Parameters border and bb is in points (unit of measure).
 - Default values for parameters:
    - orientation = landscape
    - size        = a4
    - border      = 20
    - sb          = 0
    - rb          = 0
    - rows        = 2
    - cols        = 3
    - o           = miip.pdf

#Author
Peter Sekan, uco 433390, peter.sekan@mail.muni.cz