# Format Specification

This document outlines the format specifications for replay data generated and/or used by YukiReplay.

## Overview

A file containing a full match replay should use the `.yrep` file extention. This document will refer to the file format as **the YREP format**.

Data of a replay is stored in binary format. A valid replay contains the following sections:

- A header section
- A data section consisting of multiple frames, each representing a single server tick (1/20th of a second).

## Data Types

The following data types are used in the YREP format:

- Java primitives:
  - `byte`: 8-bit signed integer (1 byte)
  - `short`: 16-bit signed integer (2 bytes)
  - `int`: 32-bit signed integer (4 bytes)
  - `long`: 64-bit signed integer (8 bytes)
  - `float`: 32-bit floating point number (4 bytes)
  - `double`: 64-bit floating point number (8 bytes)
- Strings:
  - Strings are length-prefixed.
  - A string is prefixed with a `int` that indicates the length of the string in bytes.
- UUIDs:
  - UUIDs are 16 bytes long and are stored in the following format:
    - The first 8 bytes represent the most significant bits (MSB) of the UUID.
    - The last 8 bytes represent the least significant bits (LSB) of the UUID.

All numbers are big-endian encoded.

## Header Section

A valid YREP file must start with a header section. The header section contains critical indentifying information about the replay and other optional metadata. A header section contains the following fields in the following order

- Magic (4) - `0d 00 07 21`
- Version (2) - `short` indicating the version of the YREP format
- Protocol Version - `length-prefixed string` indicating the server protocol version at the time when the replay was recorded. This is used to ensure compatibility with the server version. Uses a format of v`major`_`minor`_R`patch` (e.g. `v1_8_R3`).
- Optional metadata length (4) - Length of the following optional metadata section in bytes. If this value is 0, the optional metadata section is not present.
- Optional metadata(`Optional metadata length`)
  - The optional metadata section is a length-prefixed string that contains additional information about the replay. This is to be set by developers using the YukiReplay API.
  - The format of the optional metadata is not specified and can be used for any purpose.
  - The optional metadata section is not required and can be omitted if not needed.

The header section is immediately followed by the body section.

## Body Section

The body section contains multiple frames, each representing a single server tick (1/20th of a second). 

Frames are stored immediately next to each other, with no padding inbetween. The first frame is stored immediately after the header section. Each frame contains the following fields in the following order:

- Frame number (4) - `int` indicating the frame number. This starts at 1.
- Instruction count (2) - `short` indicating the total number of instructions in this frame.
- Zero or more instructions, each with the following fields:
  - Instruction ID (2) - `short` indicating the ID of the instruction. This is used to identify the type of instruction. Refer to the instruction ID table below for a list of valid instruction IDs.
  - Instruction length (2) - `short` indicating the length of the following instruction data in bytes.
  - Instruction data (`Instruction length`) - The instruction data is a variable-length field that contains the actual data for the instruction. The format of the instruction data depends on the instruction ID and is documented in the instruction ID table below.

YREP files do not have footers. The body section ends when the last frame ends. The end of the file is the end of the body section.