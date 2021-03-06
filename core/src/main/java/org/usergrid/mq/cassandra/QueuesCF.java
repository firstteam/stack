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

import static me.prettyprint.hector.api.ddl.ComparatorType.COUNTERTYPE;
import static org.usergrid.persistence.cassandra.CassandraPersistenceUtils.getIndexMetadata;

import java.util.List;

import me.prettyprint.hector.api.ddl.ColumnDefinition;

import org.usergrid.persistence.cassandra.CFEnum;

// Auto-generated by ApplicationCFGenerator

public enum QueuesCF implements CFEnum {

	MESSAGE_PROPERTIES("Message_Properties", "BytesType"),

	QUEUE_PROPERTIES("Queue_Properties", "BytesType"),

	QUEUE_INBOX("Queue_Inbox", "UUIDType"),

	QUEUE_DICTIONARIES("Queue_Dictionaries", "BytesType"),

	QUEUE_SUBSCRIBERS("Queue_Subscribers", "BytesType"),

	QUEUE_SUBSCRIPTIONS("Queue_Subscriptions", "BytesType"),

	CONSUMERS("MQ_Consumers", "BytesType"),

	CONSUMER_QUEUE_MESSAGES_PROPERTIES("Consumer_Queue_Messages_Properties",
			"BytesType"),

	COUNTERS("MQ_Counters", "BytesType", COUNTERTYPE.getClassName()),

	PROPERTY_INDEX(
			"MQ_Property_Index",
			"DynamicCompositeType(a=>AsciiType,b=>BytesType,i=>IntegerType,x=>LexicalUUIDType,l=>LongType,t=>TimeUUIDType,s=>UTF8Type,u=>UUIDType,A=>AsciiType(reversed=true),B=>BytesType(reversed=true),I=>IntegerType(reversed=true),X=>LexicalUUIDType(reversed=true),L=>LongType(reversed=true),T=>TimeUUIDType(reversed=true),S=>UTF8Type(reversed=true),U=>UUIDType(reversed=true))"),

	PROPERTY_INDEX_ENTRIES(
			"MQ_Property_Index_Entries",
			"DynamicCompositeType(a=>AsciiType,b=>BytesType,i=>IntegerType,x=>LexicalUUIDType,l=>LongType,t=>TimeUUIDType,s=>UTF8Type,u=>UUIDType,A=>AsciiType(reversed=true),B=>BytesType(reversed=true),I=>IntegerType(reversed=true),X=>LexicalUUIDType(reversed=true),L=>LongType(reversed=true),T=>TimeUUIDType(reversed=true),S=>UTF8Type(reversed=true),U=>UUIDType(reversed=true))"),

	;

	public final static String STATIC_MESSAGES_KEYSPACE = "Usergrid_Messages";
	public final static String APPLICATION_MESSAGES_KEYSPACE_SUFFIX = "_messages";

	private final String cf;
	private final String comparator;
	private final String validator;
	private final String indexes;

	QueuesCF(String cf, String comparator) {
		this.cf = cf;
		this.comparator = comparator;
		validator = null;
		indexes = null;
	}

	QueuesCF(String cf, String comparator, String validator) {
		this.cf = cf;
		this.comparator = comparator;
		this.validator = validator;
		indexes = null;
	}

	QueuesCF(String cf, String comparator, String validator, String indexes) {
		this.cf = cf;
		this.comparator = comparator;
		this.validator = validator;
		this.indexes = indexes;
	}

	@Override
	public String toString() {
		return cf;
	}

	@Override
	public String getColumnFamily() {
		return cf;
	}

	@Override
	public String getComparator() {
		return comparator;
	}

	@Override
	public String getValidator() {
		return validator;
	}

	@Override
	public boolean isComposite() {
		return comparator.startsWith("DynamicCompositeType");
	}

	@Override
	public List<ColumnDefinition> getMetadata() {
		return getIndexMetadata(indexes);
	}

}
