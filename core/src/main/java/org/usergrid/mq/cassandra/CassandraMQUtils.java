/*******************************************************************************
 * Copyright (c) 2010, 2011 Ed Anuff and Usergrid, all rights reserved.
 * http://www.usergrid.com
 * 
 * This file is part of Usergrid Core.
 * 
 * Usergrid Core is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * Usergrid Core is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * Usergrid Core. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.usergrid.mq.cassandra;

import static me.prettyprint.hector.api.factory.HFactory.createColumn;
import static org.usergrid.mq.Message.MESSAGE_PROPERTIES;
import static org.usergrid.mq.Queue.QUEUE_NEWEST;
import static org.usergrid.mq.Queue.QUEUE_OLDEST;
import static org.usergrid.mq.Queue.QUEUE_PROPERTIES;
import static org.usergrid.utils.ConversionUtils.bytebuffer;
import static org.usergrid.utils.ConversionUtils.getLong;
import static org.usergrid.utils.ConversionUtils.object;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import me.prettyprint.cassandra.serializers.ByteBufferSerializer;
import me.prettyprint.cassandra.serializers.DynamicCompositeSerializer;
import me.prettyprint.cassandra.serializers.LongSerializer;
import me.prettyprint.cassandra.serializers.StringSerializer;
import me.prettyprint.cassandra.serializers.UUIDSerializer;
import me.prettyprint.hector.api.beans.HColumn;
import me.prettyprint.hector.api.mutation.Mutator;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ser.ArraySerializers.ByteArraySerializer;
import org.usergrid.mq.Message;
import org.usergrid.mq.Queue;
import org.usergrid.utils.ConversionUtils;
import org.usergrid.utils.JsonUtils;

public class CassandraMQUtils {

	public static final Logger logger = Logger
			.getLogger(CassandraMQUtils.class);

	public static final StringSerializer se = new StringSerializer();
	public static final ByteBufferSerializer be = new ByteBufferSerializer();
	public static final UUIDSerializer ue = new UUIDSerializer();
	public static final ByteArraySerializer bae = new ByteArraySerializer();
	public static final DynamicCompositeSerializer dce = new DynamicCompositeSerializer();
	public static final LongSerializer le = new LongSerializer();

	/** Logger for batch operations */
	private static final Logger batch_logger = Logger
			.getLogger(CassandraMQUtils.class.getPackage().getName() + ".BATCH");

	public static void logBatchOperation(String operation, Object columnFamily,
			Object key, Object columnName, Object columnValue, long timestamp) {

		if (batch_logger.isInfoEnabled()) {
			batch_logger.info(operation + " cf=" + columnFamily + " key=" + key
					+ " name=" + columnName + " value=" + columnValue);
		}

	}

	/**
	 * Encode a message into a set of columns. JMS properties are encoded as
	 * strings and longs everything else is binary JSON.
	 * 
	 * @param message
	 * @return
	 */
	public static Map<ByteBuffer, ByteBuffer> serializeMessage(Message message) {
		if (message == null) {
			return null;
		}
		Map<ByteBuffer, ByteBuffer> columns = new HashMap<ByteBuffer, ByteBuffer>();
		for (Entry<String, Object> property : message.getProperties()
				.entrySet()) {
			if (property.getValue() == null) {
				columns.put(bytebuffer(property.getKey()), null);
			} else if (MESSAGE_PROPERTIES.containsKey(property.getKey())) {
				columns.put(bytebuffer(property.getKey()),
						bytebuffer(property.getValue()));
			} else {
				columns.put(bytebuffer(property.getKey()),
						JsonUtils.toByteBuffer(property.getValue()));
			}
		}
		return columns;
	}

	public static Mutator<ByteBuffer> addMessageToMutator(
			Mutator<ByteBuffer> m, Message message, long timestamp) {

		Map<ByteBuffer, ByteBuffer> columns = serializeMessage(message);

		if (columns == null) {
			return m;
		}

		for (Map.Entry<ByteBuffer, ByteBuffer> column_entry : columns
				.entrySet()) {
			if ((column_entry.getValue() != null)
					&& column_entry.getValue().hasRemaining()) {
				HColumn<ByteBuffer, ByteBuffer> column = createColumn(
						column_entry.getKey(), column_entry.getValue(),
						timestamp, be, be);
				m.addInsertion(bytebuffer(message.getUuid()),
						QueuesCF.MESSAGE_PROPERTIES.toString(), column);
			} else {
				m.addDeletion(bytebuffer(message.getUuid()),
						QueuesCF.MESSAGE_PROPERTIES.toString(),
						column_entry.getKey(), be, timestamp);
			}
		}

		return m;
	}

	public static Message deserializeMessage(
			List<HColumn<String, ByteBuffer>> columns) {
		Message message = null;

		Map<String, Object> properties = new HashMap<String, Object>();
		for (HColumn<String, ByteBuffer> column : columns) {
			if (MESSAGE_PROPERTIES.containsKey(column.getName())) {
				properties.put(
						column.getName(),
						object(MESSAGE_PROPERTIES.get(column.getName()),
								column.getValue()));
			} else {
				properties.put(column.getName(),
						JsonUtils.fromByteBuffer(column.getValue()));
			}
		}
		if (!properties.isEmpty()) {
			message = new Message(properties);
		}

		return message;
	}

	public static Map<ByteBuffer, ByteBuffer> serializeQueue(Queue queue) {
		if (queue == null) {
			return null;
		}
		Map<ByteBuffer, ByteBuffer> columns = new HashMap<ByteBuffer, ByteBuffer>();
		for (Entry<String, Object> property : queue.getProperties().entrySet()) {
			if (property.getValue() == null) {
				continue;
			}
			if (Queue.QUEUE_ID.equals(property.getKey())
					|| QUEUE_NEWEST.equals(property.getKey())
					|| QUEUE_OLDEST.equals(property.getKey())) {
				continue;
			}
			if (QUEUE_PROPERTIES.containsKey(property.getKey())) {
				columns.put(bytebuffer(property.getKey()),
						bytebuffer(property.getValue()));
			} else {
				columns.put(bytebuffer(property.getKey()),
						JsonUtils.toByteBuffer(property.getValue()));
			}
		}
		return columns;
	}

	public static Queue deserializeQueue(
			List<HColumn<String, ByteBuffer>> columns) {
		Queue queue = null;

		Map<String, Object> properties = new HashMap<String, Object>();
		for (HColumn<String, ByteBuffer> column : columns) {
			if (QUEUE_PROPERTIES.containsKey(column.getName())) {
				properties.put(
						column.getName(),
						object(QUEUE_PROPERTIES.get(column.getName()),
								column.getValue()));
			} else {
				properties.put(column.getName(),
						JsonUtils.fromByteBuffer(column.getValue()));
			}
		}
		if (!properties.isEmpty()) {
			queue = new Queue(properties);
		}

		return queue;
	}

	public static Mutator<ByteBuffer> addQueueToMutator(Mutator<ByteBuffer> m,
			Queue queue, long timestamp) {

		Map<ByteBuffer, ByteBuffer> columns = serializeQueue(queue);

		if (columns == null) {
			return m;
		}

		for (Map.Entry<ByteBuffer, ByteBuffer> column_entry : columns
				.entrySet()) {
			if ((column_entry.getValue() != null)
					&& column_entry.getValue().hasRemaining()) {
				HColumn<ByteBuffer, ByteBuffer> column = createColumn(
						column_entry.getKey(), column_entry.getValue(),
						timestamp, be, be);
				m.addInsertion(bytebuffer(queue.getUuid()),
						QueuesCF.QUEUE_PROPERTIES.toString(), column);
			} else {
				m.addDeletion(bytebuffer(queue.getUuid()),
						QueuesCF.QUEUE_PROPERTIES.toString(),
						column_entry.getKey(), be, timestamp);
			}
		}

		return m;
	}

	public static ByteBuffer getQueueShardRowKey(UUID uuid, long ts) {
		ByteBuffer bytes = ByteBuffer.allocate(24);
		bytes.putLong(uuid.getMostSignificantBits());
		bytes.putLong(uuid.getLeastSignificantBits());
		bytes.putLong(ts);
		return (ByteBuffer) bytes.rewind();
	}

	public static UUID getUUIDFromRowKey(ByteBuffer bytes) {
		return ConversionUtils.uuid(bytes);
	}

	public static long getLongFromRowKey(ByteBuffer bytes) {
		bytes = bytes.slice();
		return getLong(16);
	}
}
