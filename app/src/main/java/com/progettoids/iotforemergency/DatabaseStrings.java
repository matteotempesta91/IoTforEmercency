package com.progettoids.iotforemergency;

/**
 * Created by matteotempesta on 02/03/17.
 */

public class DatabaseStrings
{
    /*
    CAMPI NODO
     */
    public static final String FIELD_NODO_CODICE = "codice";
    public static final String FIELD_NODO_POSIZIONE_X = "posizione_x";
    public static final String FIELD_NODO_POSIZIONE_Y = "posizione_y";
    public static final String FIELD_NODO_STATO = "stato";
    public static final String FIELD_NODO_QUOTA = "quota";
    public static final String FIELD_NODO_ORARIO_ULTIMA_RICEZIONE = "orario_ultima_ricezione"; // va su beacon


    /*
    CAMPI BEACON
     */
    public static final String FIELD_BEACON_MAC = "mac_beacon";
    public static final String FIELD_BEACON_CODICE_NODO= "codice_nodo_beacon";
    public static final String FIELD_BEACON_TEMPERATURA = "temperatura";
    public static final String FIELD_BEACON_ACCELERAZIONEX = "accelerazionex";
    public static final String FIELD_BEACON_ACCELERAZIONEY = "accelerazioney";
    public static final String FIELD_BEACON_ACCELERAZIONEZ = "accelerazionez";
    public static final String FIELD_BEACON_UMIDITA = "umidita";
    public static final String FIELD_BEACON_LUMINOSITA = "luminosita";
    public static final String FIELD_BEACON_PRESSIONE = "pressione";



    public static final String TBL_NAME_NODO = "Nodo";
    public static final String TBL_NAME_BEACON = "Beacon";




    public static final String[] mac_beacon = {
            "B0:B4:48:BD:93:82"
    };
    public static final String[] codice_nodo_beacon = {
            "145DICEA"
    };


    public static final int[] posizione_x = {
            71,
            87,
            90,
            100,
            119,
            133,
            133,
            133,
            145,
            91,
            143,
            87,
            90,
            130,
            133,
            135,
            109,
            63,
            85,
            112,
            145,
            87,
            87,
            90,
            69,
            135,
            135,
            151,
            154,
            157,
            129,
            110,
            92,
            144,
            63,
            152,
            100,
            100,
            150,
            147,
            163,
            91,
            100,
            107,
            117,
            125,
            133,
            136,
            147,
            149,
            154,
            159,
            159,
            160,
            140,
            86,
            158,
            160,
            91,
            144,
            62,
            154,
            136
    };

    public static final int[] posizione_y= {
            465,
            465,
            480,
            465,
            465,
            480,
            465,
            471,
            471,
            484,
            473,
            482,
            487,
            454,
            482,
            454,
            482,
            465,
            454,
            482,
            475,
            465,
            456,
            472,
            464,
            470,
            456,
            485,
            474,
            485,
            465,
            465,
            484,
            474,
            464,
            456,
            456,
            453,
            500,
            500,
            445,
            467,
            467,
            467,
            467,
            467,
            467,
            456,
            456,
            472,
            456,
            472,
            456,
            450,
            471,
            465,
            482,
            445,
            484,
            474,
            465,
            454,
            454
    };


    public static final int[] quota = {
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            145,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            150,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155,
            155
    };


    public static final String[] codice = {
            "145DICEA",
            "145S1",
            "145RG1",
            "145R3",
            "145R1",
            "145RG2",
            "145WC1",
            "145S2",
            "145S3",
            "145A5",
            "145A3",
            "145U1",
            "145UA5",
            "145U3",
            "145U2",
            "145RAM",
            "145EMG1",
            "145EMA7",
            "145EM1",
            "145EMG2",
            "145EMA3",
            "150G1",
            "150STRADE",
            "150DICEA",
            "150DICEA1",
            "150R2",
            "150WC1",
            "150RL",
            "150S1",
            "150R1",
            "150G2",
            "150G1G2",
            "150A5",
            "150A3",
            "150A7",
            "150RAM",
            "150EMA7",
            "150EM1",
            "150EMRL",
            "150EMR1",
            "150BIB",
            "155R567",
            "155R4",
            "155R4D3",
            "155R23D2",
            "155RD1",
            "155WC1",
            "155EM1",
            "155S1",
            "155WC2",
            "155EM3",
            "155CESMI",
            "155ACQ",
            "155EM4",
            "155ECDL",
            "155DICEA",
            "155BAR",
            "155UP",
            "155A5",
            "155A3",
            "155A7",
            "155EM2",
            "155U1"
    };
}

