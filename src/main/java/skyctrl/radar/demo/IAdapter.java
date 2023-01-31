package skyctrl.radar.demo;

public interface IAdapter {
    /*
    $RS,<1>,<2>,<3>,<4>,<5>,<6>,<7>,<8>,<9>,<10>,<11>,<12>,<13>,<14>,<15>,<16>,<17>,
<18>,<19>,<20>*hh<CR><LF>
F.ID    F.Name          Format      Unit                    Description
1   Target Range        x.xx        m (two decimal places)  Target Range
2   Reserved field      -           -                       Filled with NULL.
3   Target Azimuth      xxx.xx      0.0 to 359.99 (degrees) Target Azimuth Position Relative to
                                                            predefined 0 position
4   Target Elevation    xxx.xx      0.0 to 359.99 (degrees) Target Elevation Angle from
                                                            Ground Level
5   Target Location,X   x.xx        m (two decimal places)  Location of Target in Cartesian
                                                            coordinates, relative to the Radar.
6   Target Location,Y   x.xx        m (two decimal places)
7   Target Location, Z  x.xx        m (two decimal places)
8   Target Range Rate   x.xx        m/s (two decimal places)    Doppler Range Rate
9   Reserved field      -           -                       Filled with NULL.
10  Reserved field      -           -
11  Reserved field      -           -
12  Target
    Classification      x           Integer value           Classification from UNDEFINED
                                                            (0), DRONE (20), HIGH RCS (30).
13  Target RCS          x.xx        m2 (two decimal places) Radar Cross Section
14  Track ID            x           Integer                 Unique Track ID for each Target
15  Reserved field      -           -                       Filled with NULL.
16  Reserved field      -           -
17  Reserved field      -           -
18  Latitude            xx.xxxxxxx  degrees (7 decimal places)  Latitude Position
19  Longitude           xx.xxxxxxx  degrees (7 decimal places)  Longitude Position
20  Time of Data        hhmmss.sss  In ms precision
                                    (3 d.p seconds)         GPS UTC time
     */
    public void processing(String rawData);
}
