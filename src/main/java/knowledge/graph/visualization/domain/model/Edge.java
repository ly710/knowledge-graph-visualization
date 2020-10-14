package knowledge.graph.visualization.domain.model;

import java.util.Arrays;

public class Edge extends AbstractModel {
    private Long SourceId;

    private Long targetId;

    private String sourceLabel;

    private String targetLabel;

    private Double sourceSize;

    private Double targetSize;

    private String edgeLabel;

    private byte[] position;

    private Double sourceX;

    private Double sourceY;

    private Double targetX;

    private Double targetY;

    public void setPosition(byte[] position) {
        double[] points = bytes2Points(position);

        sourceX = points[0];
        sourceY = points[1];
        targetX = points[2];
        targetY = points[3];
        this.position = position;
    }

    public Long getSourceId() {
        return SourceId;
    }

    public void setSourceId(Long sourceId) {
        SourceId = sourceId;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    public String getTargetLabel() {
        return targetLabel;
    }

    public void setTargetLabel(String targetLabel) {
        this.targetLabel = targetLabel;
    }

    public String getEdgeLabel() {
        return edgeLabel;
    }

    public void setEdgeLabel(String edgeLabel) {
        this.edgeLabel = edgeLabel;
    }

    public byte[] getPosition() {
        return position;
    }

    public Double getSourceX() {
        return sourceX;
    }

    public void setSourceX(Double sourceX) {
        this.sourceX = sourceX;
    }

    public Double getSourceY() {
        return sourceY;
    }

    public void setSourceY(Double sourceY) {
        this.sourceY = sourceY;
    }

    public Double getTargetX() {
        return targetX;
    }

    public void setTargetX(Double targetX) {
        this.targetX = targetX;
    }

    public Double getTargetY() {
        return targetY;
    }

    public void setTargetY(Double targetY) {
        this.targetY = targetY;
    }

    public static double[] bytes2Points(byte[] arr){
        if(arr==null){
            return null;
        }
        if(arr.length==25){
            return bytes2OnePoint(arr);
        }
        return bytes2MutiPoints(arr);
    }

    private static double bytes2Double(byte[] arr,int start) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[start+i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    private static double[] bytes2OnePoint(byte[] arr){
        return new double[]{bytes2Double(arr,9),bytes2Double(arr,17)};
    }

    private static double[] bytes2MutiPoints(byte[] arr){
        int len=(arr.length-13)/8;
        double[] result=new double[len];
        for(int i=0;i<len;++i){
            result[i]=bytes2Double(arr,13+i*8);
        }
        return result;
    }

    public Double getSourceSize() {
        return sourceSize;
    }

    public void setSourceSize(Double sourceSize) {
        this.sourceSize = sourceSize;
    }

    public Double getTargetSize() {
        return targetSize;
    }

    public void setTargetSize(Double targetSize) {
        this.targetSize = targetSize;
    }
}
