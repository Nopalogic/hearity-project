FROM apache/airflow:latest-python3.12

USER root

RUN apt-get update && \
    apt-get -y install git && \
    apt-get clean

RUN chmod -R 777 /opt/airflow/dags /opt/airflow/log

USER airflow

COPY requirements.txt /tmp/requirements.txt
RUN pip install -r /tmp/requirements.txt
