# Stage 1: Build the C++ binary
FROM alpine:3.18 AS builder
RUN apk add --no-cache g++ make cmake git

WORKDIR /app
COPY . .

RUN mkdir build && cd build && \
    cmake -DCMAKE_BUILD_TYPE=Release .. && \
    make

# Stage 2: Run the binary in an ultra-lean container
FROM alpine:3.18
RUN apk add --no-cache libstdc++

WORKDIR /root/
COPY --from=builder /app/build/hardware_simulator .

CMD ["./hardware_simulator"]