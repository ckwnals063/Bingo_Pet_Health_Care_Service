package kr.co.jmsmart.bingo.data;

import java.nio.ByteBuffer;

import kr.co.jmsmart.bingo.data.Sleepdoc_10_min_data_type;

/**
 * Created by parkboo on 2015. 10. 6..
 */
public class Sleepdoc_ext_interface_data_type {
    public Sleepdoc_10_min_data_type[] d = new Sleepdoc_10_min_data_type[6];
    public int time_zone;
    public short reset_num;
    public int remainings;

    public Sleepdoc_ext_interface_data_type() {
    }

    public Sleepdoc_ext_interface_data_type(byte[] bytes) {
        int i = 0;
        byte[] time_zone_bytes = new byte[4];
        byte[] reset_num_bytes = new byte[2];
        byte[] remainings_bytes = new byte[4];

        for(i=0; i<6; i++) {
            byte[] data = new byte[24];
            System.arraycopy(bytes, i*24, data, 0, 24 );
            d[i] = new Sleepdoc_10_min_data_type(data);
        }

        int srcPos = Sleepdoc_10_min_data_type.size() * 6;

        System.arraycopy(bytes, srcPos, time_zone_bytes, 0, 4 );
        srcPos += 4;

        System.arraycopy(bytes, srcPos, reset_num_bytes, 0, 2 );
        srcPos += 2;

        System.arraycopy(bytes, srcPos, remainings_bytes, 0, 4 );

        ByteBuffer wrapped = ByteBuffer.wrap(time_zone_bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        this.time_zone = wrapped.getInt();

        wrapped = ByteBuffer.wrap(reset_num_bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        this.reset_num = wrapped.getShort();

        wrapped = ByteBuffer.wrap(remainings_bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN);
        this.remainings = wrapped.getInt();
    }

    public static int size() {
        int sleepdoc_10_min_data_type_size = Sleepdoc_10_min_data_type.size();
        return sleepdoc_10_min_data_type_size * 6 + 10;
    }

}
