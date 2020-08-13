package com.newrelic.jfr.parser;

import com.newrelic.jfr.model.ChunkHeader;
import com.newrelic.jfr.model.ChunkMetadata;
import com.newrelic.jfr.model.ConstantPool;
import com.newrelic.jfr.model.PoolDescriptor;
import com.newrelic.jfr.model.TypeDescriptor;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConstantPoolParser {

    private final ByteSupport bs = new ByteSupport();
    private final TypeDescriptorIndexer typeDescriptorIndexer = new TypeDescriptorIndexer();

    public Map<Long,ConstantPool> parse(ByteBuffer buff, ChunkHeader header, ChunkMetadata meta) {

        var offset = header.getConstantPoolOffset();
        var pool = parsePoolDescriptor(buff, meta, (int) offset);

        List<PoolDescriptor> poolDescriptors = Stream.iterate(pool,
                pd -> (pd == pool) || (pd.getOffsetToNextPool() != 0),
                pd -> parsePoolDescriptor(buff, meta, (int) (pd.getOffset() + pd.getOffsetToNextPool()))
        ).collect(Collectors.toList());

//        parsePoolDescriptor()

                //TODO: Read all pool descriptors at all start offsets

                Map < Integer, TypeDescriptor > types = typeDescriptorIndexer.index(meta);
//        Map<Integer, MetadataTreeNode> indexed = meta.getMetadataChildrenIndexedById();


//        parsePools(buff, types);


//        var typeId = (int) bs.readVarLen(buff);
//        var numConstants = (int)bs.readVarLen(buff);
//        var key = bs.readVarLen(buff);

        return null;//new ConstantPool();
    }

    private PoolDescriptor parsePoolDescriptor(ByteBuffer buff, ChunkMetadata meta, int offset) {
        buff.position(offset);

        var size = (int) bs.readVarLen(buff);
        var typeId = (int) bs.readVarLen(buff);
        var timestamp = bs.readVarLen(buff);
        var duration = bs.readVarLen(buff);

        var offsetToNextPool = bs.readVarLen(buff);
        var flush = bs.readVarLen(buff) != 0;   //?????

        var numPools = (int)bs.readVarLen(buff);
        for(int i = 0 ; i < numPools; i++){
            long currentTypeId = bs.readVarLen(buff);
            var numKvPairs = (int)bs.readVarLen(buff);
            for(int j= 0 ; j < numKvPairs; j++){
//                var key = bs.readVarLen(buff);
                Object value = null;//GOTTA PARSE IT TOO
            }
        }

        return new PoolDescriptor(offset, size, typeId, timestamp, duration, offsetToNextPool, flush);
    }


    private void parsePools(ByteBuffer buff, Map<Integer, TypeDescriptor> types) {
        var ct = bs.readVarLen(buff);
        System.out.println("**DEBUG: parsing " + ct + " pools");
        for (int i = 0; i < ct; i++) {
            var typeId = (int)bs.readVarLen(buff);
            TypeDescriptor type = types.get(typeId);

            parsePoolTypes(buff, type, types);
            typeId = 0;
//            var constantPool = new ConstantPoolParser().parse(buff, header, meta);
        }
    }

    private void parsePoolTypes(ByteBuffer buff, TypeDescriptor type, Map<Integer, TypeDescriptor> types) {
        var ct = (int)bs.readVarLen(buff);
        for (int i = 0; i < ct; i++){
            parseType(buff, type, types);
        }
    }

    private void parseType(ByteBuffer buff, TypeDescriptor type, Map<Integer, TypeDescriptor> types) {
        if(type == null || type.getFields() == null){
            System.out.println("Help me.");
            return;
        }
        type.getFields().forEach(field -> {
            var fieldType = types.get(field.getClassId());
            parseType(buff, fieldType, types);
        });
    }
}
