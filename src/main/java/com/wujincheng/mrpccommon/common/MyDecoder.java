package com.wujincheng.mrpccommon.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.wujincheng.mrpccommon.common.MyProtocol.BASE_LENGTH;


public class MyDecoder extends ByteToMessageDecoder {

    private static final Logger logger= LoggerFactory.getLogger(MyDecoder.class);
    //private static final Map<String,Class<?>> classMap=new ConcurrentHashMap<String, Class<?>>();

    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf buffer, List<Object> out) throws Exception {
        while (buffer.isReadable()){
            if (buffer.readableBytes() < BASE_LENGTH) {
                return;
            }
            int beginReader;
            byte[] magicData;
            while (true) {
                beginReader = buffer.readerIndex();
                //buffer.markReaderIndex();
                magicData=new byte[2];
                buffer.readBytes(magicData);
                if (magicData[0] == MyProtocol.HIGH_HEAD_DATA&&magicData[1] == MyProtocol.LOW_HEAD_DATA) {
                    break;
                }
                //buffer.resetReaderIndex();
                //buffer.readByte();
                buffer.readerIndex(beginReader+1);
                if (buffer.readableBytes() < BASE_LENGTH) {
                    return;
                }
            }
            int length = buffer.readInt();
            if (buffer.readableBytes() < length) {
                buffer.readerIndex(beginReader);
                return;
            }
            byte[] data = new byte[length];
            buffer.readBytes(data);
//        ByteArrayInputStream is = new ByteArrayInputStream(data);
            try {
//
//            //Hessian的反序列化读取对象
//            Hessian2Input hessianInput = new Hessian2Input(is);
//            long id=hessianInput.readLong();
//            int type=hessianInput.readInt();
//            Map<String,String> map=(Map<String,String>)hessianInput.readObject(Map.class);
//            String clzName=hessianInput.readString();
//            Object responseData=null;
//            if(clzName!=null){
//                if(type==Common.WEN_JIAN){
//                    responseData = hessianInput.readObject();
//                }else{
//                    Class<?> clz=null;
//                    if(classMap.containsKey(clzName)){
//                        clz=classMap.get(clzName);
//                    }else{
//                        clz=Class.forName(clzName);
//                        classMap.put(clzName,clz);
//                    }
//                    responseData = hessianInput.readObject(clz);
//                }
//
//            }
//
//            Response response=new Response(id,type,clzName,responseData);
//            response.setMap(map);
                Request request=ProtostuffUtils.deserialize(data,Request.class);
                Response response= new Response(request.getId(),request.getType()
                        ,request.getDataType(),request.getData());
                response.setMap(request.getMap());
                response.setClassName(request.getClassName());
                response.setHasExecption(request.getHasExecption());
                response.setParameter(request.getParameter());
                response.setParameterClassType(request.getParameterClassType());
                response.setThrowable(request.getThrowable());
                response.setMethodName(request.getMethodName());
                response.setAttachments(request.getAttachments());
                out.add(response);

            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }//finally {
//            if(is.available()>0){
//                System.out.println("skip :"+is.available());
//                is.skip(is.available());
//            }
            //}
        }


    }
}
