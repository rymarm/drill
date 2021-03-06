/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.store.cassandra.schema;

import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.cassandra.CassandraSchema;
import org.apache.calcite.adapter.cassandra.CassandraTable;
import org.apache.calcite.linq4j.tree.Expression;
import org.apache.calcite.linq4j.tree.Expressions;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.Table;
import org.apache.calcite.util.BuiltInMethod;
import org.apache.drill.exec.planner.logical.DrillTable;
import org.apache.drill.exec.store.AbstractSchema;
import org.apache.drill.exec.store.StoragePlugin;
import org.apache.drill.exec.store.cassandra.CassandraStorageConfig;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CassandraDrillSchema extends AbstractSchema {
  private final CassandraSchema delegatingSchema;
  private final StoragePlugin plugin;
  private final Map<String, Table> tables = new ConcurrentHashMap<>();

  public CassandraDrillSchema(String name, StoragePlugin plugin, CassandraSchema delegatingSchema) {
    super(Collections.emptyList(), name);
    this.plugin = plugin;
    this.delegatingSchema = delegatingSchema;
  }

  @Override
  public String getTypeName() {
    return CassandraStorageConfig.NAME;
  }

  @Override
  public Table getTable(String tableName) {
    return tables.computeIfAbsent(tableName, this::getDrillTable);
  }

  private DrillTable getDrillTable(String tableName) {
    CassandraTable table = (CassandraTable) delegatingSchema.getTable(tableName);
    return table == null ? null
        : new CassandraDynamicTable(plugin, tableName, null, table);
  }

  @Override
  public Set<String> getTableNames() {
    return delegatingSchema.getTableNames();
  }

  @Override
  public Expression getExpression(SchemaPlus parentSchema, String name) {
    return Expressions.call(
        DataContext.ROOT,
        BuiltInMethod.DATA_CONTEXT_GET_ROOT_SCHEMA.method);
  }

  public CassandraSchema getDelegatingSchema() {
    return delegatingSchema;
  }
}
