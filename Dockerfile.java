# radanalytics-java-spark
FROM fabric8/s2i-java

MAINTAINER Trevor McKay tmckay@redhat.com

ARG SPARK_VERSION=2.1.1
ARG HADOOP_VERSION=2.7

LABEL io.k8s.description="Platform for building a radanalytics java spark app" \
      io.k8s.display-name="radanalytics java_spark" \
      io.openshift.expose-services="8080:http" \
      io.openshift.s2i.scripts-url="image:///usr/local/s2i" \
      io.openshift.tags="builder,radanalytics,java_spark"

USER root

RUN yum install -y tar java golang make nss_wrapper git gcc && \
    yum clean all

RUN cd /opt && \
    curl https://dist.apache.org/repos/dist/release/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz | \
        tar -zx && \
    ln -s spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} spark

ENV RADANALYTICS_JAVA_SPARK=1.0 GOPATH=/go APP_ROOT=/opt/app-root PATH=$PATH:/opt/spark/bin SPARK_HOME=/opt/spark
ADD . /go/src/github.com/radanalyticsio/oshinko-s2i
ADD ./common/spark-conf/* ${SPARK_HOME}/conf/
ADD ./common/generate_container_user ${APP_ROOT}/etc/

RUN mkdir -p $APP_ROOT/src && \
    cd /go/src/github.com/radanalyticsio/oshinko-s2i/java && \
    make utils && \
    cp utils/* $APP_ROOT/src && \
    chown -R 1001:0 $APP_ROOT && \
    chmod a+rwX -R $APP_ROOT && \
    cp s2i/bin/* /usr/local/s2i && \
    chown -R 1001:0 /opt/spark/conf && \
    chmod g+rw -R /opt/spark/conf && \
    rm -rf /go/src/github.com/radanalyticsio/oshinko-s2i/common/oshinko-cli

USER 1001
CMD /usr/local/s2i/usage
