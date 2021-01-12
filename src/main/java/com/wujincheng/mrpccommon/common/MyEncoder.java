package com.wujincheng.mrpccommon.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;

public class MyEncoder extends MessageToByteEncoder<Request> {




    protected void encode(ChannelHandlerContext ctx, Request request, ByteBuf out) throws Exception {
        //System.out.println(msg.getClass());

        if(request.getData()!=null&&request.getDataType()==null){
            request.setDataType(request.getData().getClass().getName());
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        Hessian2Output ho = new Hessian2Output(os);
//        ho.writeLong(request.getId());
//        ho.writeInt(request.getType());
//        ho.writeObject(request.getMap());
//        if(request.getDataType()!=null){
//            ho.writeString(request.getDataType());
//            ho.writeObject(request.getData());
//        }
//        ho.flush();
//        byte[] data=os.toByteArray();
        byte[] data= ProtostuffUtils.serialize(request);

        //out.writeInt(MyProtocol.HEAD_DATA);
        out.writeBytes(MyProtocol.HIGH_LOW_HEAD_DATA);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
