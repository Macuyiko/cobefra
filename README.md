# CoBeFra: A Comprehensive Benchmarking Framework for Conformance Checking

## About

> CoBeFra is a comprehensive benchmarking suite to set up large scale conformance checking experiments and is used and loved by many researchers in the field.

Within the process mining research area, two prominent tasks can be discerned. First of all, process discovery deals with the automatic construction of a process model out of an event log. Secondly, conformance checking focuses on the assessment of the quality of a discovered or designed process model in respect to the actual behavior as captured in event logs.

Concerning conformance checking, multiple techniques and metrics have been developed and described in the literature. However, the process mining domain lacks a comprehensive framework for assessing the goodness of a process model from a quantitative perspective.

With CoBeFra, we offer the architecture for an extensible framework within ProM, allowing for the consistent, comparative and repeatable calculation of conformance metrics. For the development and assessment of both process discovery as well as conformance techniques, such a framework is greatly valuable.

## Included Metrics

The current version of CoBeFra is able to open logs stored in either the XES or MXML file format. Only Petri nets (PNML) can be used as process models for now. Note that most process models can be converted to Petri nets using ProM.

The current version of CoBeFra ships with the following metrics:

- Fitness (Rozinat et al.)
- Succesful Execution (Rozinat et al.)
- Proper Completion (Rozinat et al.)
- Simple Behavioral Appropriateness (Rozinat et al.)
- Advanced Structural Appropriateness (Rozinat et al.)
- Simple Behavioral Appropriateness (Rozinat et al.)
- Advanced Structural Appropriateness (Rozinat et al.)
- Behavioral Recall (Goedertier et al.)
- (Weighted) Behavioral Precision (vanden Broucke et al.)
- (Weighted) Behavioral Generalization (vanden Broucke et al.)
- Alignment-Based Fitness (Adriansyah et al.)
- ETC Precision (Munoz-Gama et al.)
- One Align Precision (Adriansyah et al.)
- Best Align Precision (Adriansyah et al.)
- Alignment Based Precision (Adriansyah et al.)
- Alignment Based Probabilistic Generalization (Adriansyah et al.)
- Anti-Alignment Based Precision (van Dongen et al.)
- Anti-Alignment Based Generalization (van Dongen et al.)
- Behavioral Profile Conformance metrics (Weidlich et al.)
- Various simplicity metrics (Mendling et al.)

## Citation

vanden Broucke, S., De Weerdt, J., Vanthienen, J., Baesens, B. (2013). A comprehensive benchmarking framework (CoBeFra) for conformance analysis between procedural process models and event logs in ProM. Proceedings of the IEEE Symposium on Computational Intelligence and Data Mining, CIDM 2013, part of the IEEE Symposium Series on Computational Intelligence 2013, SSCI 2013. IEEE Symposium on Computational Intelligence and Data Mining (CIDM 2013). Singapore, 16-19 April 2013 (pp. 254-261). New York, USA: IEEE.

For more information, see http://processmining.be/cobefra/