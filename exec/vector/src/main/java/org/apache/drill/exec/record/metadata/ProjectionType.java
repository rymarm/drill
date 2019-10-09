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
package org.apache.drill.exec.record.metadata;

import org.apache.drill.common.types.TypeProtos.DataMode;
import org.apache.drill.common.types.TypeProtos.MajorType;
import org.apache.drill.common.types.TypeProtos.MinorType;

public enum ProjectionType {
  UNPROJECTED,
  WILDCARD,     // *
  UNSPECIFIED,  // x
  SCALAR,       // x (from schema)
  TUPLE,        // x.y
  ARRAY,        // x[0]
  TUPLE_ARRAY,  // x[0].y
  DICT, // x[0] or x['key'] (depends on key type)
  DICT_ARRAY; // x[0][42] or x[0]['key'] (depends on key type)

  public boolean isTuple() {
    return this == ProjectionType.TUPLE || this == ProjectionType.TUPLE_ARRAY;
  }

  public boolean isArray() {
    return this == ProjectionType.ARRAY || this == ProjectionType.TUPLE_ARRAY || this == DICT_ARRAY;
  }

  public boolean isDict() {
    return this == DICT || this == DICT_ARRAY;
  }

  public boolean isMaybeScalar() {
    return this == UNSPECIFIED || this == SCALAR;
  }

  public static ProjectionType typeFor(MajorType majorType) {
    if (majorType.getMinorType() == MinorType.MAP) {
      if (majorType.getMode() == DataMode.REPEATED) {
        return TUPLE_ARRAY;
      } else {
        return TUPLE;
      }
    }
    if (majorType.getMinorType() == MinorType.DICT) {
      if (majorType.getMode() == DataMode.REPEATED) {
        return DICT_ARRAY;
      } else {
        return DICT;
      }
    }
    if (majorType.getMode() == DataMode.REPEATED) {
      return ARRAY;
    }
    return SCALAR;
  }

  public boolean isCompatible(ProjectionType other) {
    switch (other) {
    case UNPROJECTED:
    case UNSPECIFIED:
    case WILDCARD:
      return true;
    default:
      break;
    }

    switch (this) {
    case ARRAY:
      return other == ARRAY || other == TUPLE_ARRAY
          || other == DICT // the actual key type should be validated later
          || other == DICT_ARRAY;
    case TUPLE_ARRAY:
      return other == TUPLE_ARRAY || other == DICT_ARRAY;
    case SCALAR:
      return other == SCALAR;
    case TUPLE:
      return other == TUPLE || other == TUPLE_ARRAY || other == DICT || other == DICT_ARRAY;
    case DICT:
      return other == DICT || other == DICT_ARRAY;
    case UNPROJECTED:
    case UNSPECIFIED:
    case WILDCARD:
      return true;
    default:
      throw new IllegalStateException(toString());
    }
  }
}
