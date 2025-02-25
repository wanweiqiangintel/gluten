# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

find_arrow_lib(${PARQUET_LIB_NAME})
function(add_velox_test TEST_EXEC)
  set(options)
  set(one_value_args)
  set(multi_value_args
    SOURCES
  )
  cmake_parse_arguments(ARG
  "${options}"
  "${one_value_args}"
  "${multi_value_args}"
  ${ARGN})

  if(ARG_SOURCES)
    set(SOURCES ${ARG_SOURCES})
  else()
    message(FATAL_ERROR "No sources specified for test ${TEST_NAME}")
  endif()
  add_executable(${TEST_EXEC} ${SOURCES})
  target_include_directories(${TEST_EXEC} PRIVATE ${CMAKE_SOURCE_DIR}/velox ${CMAKE_SOURCE_DIR}/src)
  target_link_libraries(${TEST_EXEC} gflags Arrow::parquet velox GTest::gtest GTest::gtest_main glog::glog benchmark::benchmark simdjson)
  gtest_discover_tests(${TEST_EXEC})
endfunction()

add_velox_test(velox_shuffle_writer_test SOURCES VeloxShuffleWriterTest.cc)
add_velox_test(velox_data_conversion_test SOURCES ArrowToVeloxTest.cc VeloxColumnarToRowTest.cc VeloxRowToColumnarTest.cc ColumnarToRowTest.cc)
add_velox_test(orc_test SOURCES OrcTest.cc)
add_velox_test(velox_operators_test SOURCES VeloxColumnarBatchSerializerTest.cc)
add_velox_test(
  velox_plan_conversion_test
  SOURCES
  Substrait2VeloxPlanConversionTest.cc
  Substrait2VeloxPlanValidatorTest.cc
  Substrait2VeloxValuesNodeConversionTest.cc
  SubstraitExtensionCollectorTest.cc
  VeloxSubstraitRoundTripTest.cc
  VeloxSubstraitSignatureTest.cc
  VeloxToSubstraitTypeTest.cc
  FunctionTest.cc
  JsonToProtoConverter.cc
  FilePathGenerator.cc)
