# PySpark Notebook Setup Guide

## Install Python

### Install Anaconda

- Download anaconda
    ```bash
    wget https://repo.anaconda.com/archive/Anaconda3-5.2.0-Linux-x86_64.sh
    ```
- Install
    Reference: https://conda.io/docs/user-guide/install/macos.html#install-macos-silent
    ```bash
    sh Anaconda3-5.2.0-Linux-x86_64.sh -b -p . -f
    ```

### Configure environment

```bash
conda create -n bigdata python=3.6 anaconda
source activate bigdata
# useful to have nice tables of contents in the notebooks, but they are not required.
conda install -n bigdata -c conda-forge jupyter_contrib_nbextensions
# If you want to use the Jupyter extensions (optional, they are mainly useful to have nice tables of contents), you first need to install them:
jupyter contrib nbextension install --user
# Then you can activate an extension, such as the Table of Contents (2) extension:
jupyter nbextension enable toc2/main
# Okay! You can now start Jupyter, simply type:
jupyter notebook
```

Note: you can also visit [http://localhost:8888/nbextensions](http://localhost:8888/nbextensions) to activate and configure Jupyter extensions.

### Configure jupyter

```bash
# configure jupyter and prompt for password
jupyter notebook --generate-config
jupass=`python -c "from notebook.auth import passwd; print(passwd())"`
echo "c.NotebookApp.password = u'"$jupass"'" >> $HOME/.jupyter/jupyter_notebook_config.py
echo "c.NotebookApp.ip = '*'
c.NotebookApp.open_browser = False" >> $HOME/.jupyter/jupyter_notebook_config.py
```

## PySpark

There are two ways to get PySpark available in a Jupyter Notebook:

1. Configure PySpark driver to use Jupyter Notebook: running `pyspark` will automatically open a Jupyter Notebook

    ```bash
    source activate bigdata
    export PYSPARK_DRIVER_PYTHON=jupyter
    export PYSPARK_DRIVER_PYTHON_OPTS='notebook'
    # specify python used by spark cluster
    export PYSPARK_PYTHON=`which python`
    pyspark --master yarn
    ```

2. Load a regular Jupyter Notebook and load PySpark using findSpark package

    use `findSpark` package to make a Spark Context available in your code.

    - Install `findspark` 

    ```bash
    pip install findspark
    ```

    - Start `jupyter notebook`

        ```bash
        jupyter notebook
        ```
    - Run following code in `notebook`

        ```python
        import findspark
        findspark.init() # or findspark.init('/path/to/spark_home')

        import pyspark
        sc = pyspark.SparkContext(appName="myAppName")
        ```

## Reference
[https://blog.sicara.com/get-started-pyspark-jupyter-guide-tutorial-ae2fe84f594f](https://blog.sicara.com/get-started-pyspark-jupyter-guide-tutorial-ae2fe84f594f)