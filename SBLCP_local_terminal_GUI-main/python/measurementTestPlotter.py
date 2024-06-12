import csv
import sys
import plotly.graph_objects as go
from datetime import datetime

def plot_real_time_data(name_of_plot):
    test_count = []
    phaseA_yoko_current = []
    phaseA_breaker_current = []
    phaseB_yoko_current = []
    phaseB_breaker_current = []
    phaseA_percentage_difference = []
    phaseB_percentage_difference = []

    fig_current = go.Figure()
    fig_diff = go.Figure()

    fig_current.add_trace(go.Scatter(x=test_count, y=phaseA_yoko_current, name='Phase A Yoko Current'))
    fig_current.add_trace(go.Scatter(x=test_count, y=phaseA_breaker_current, name='Phase A Breaker Current'))
    fig_current.add_trace(go.Scatter(x=test_count, y=phaseB_yoko_current, name='Phase B Yoko Current'))
    fig_current.add_trace(go.Scatter(x=test_count, y=phaseB_breaker_current, name='Phase B Breaker Current'))

    fig_current.update_layout(
        title=name_of_plot,
        xaxis_title='Ideal Current',
        yaxis_title='Current (A)'
    )

    fig_diff.add_trace(go.Scatter(x=test_count, y=phaseA_percentage_difference, name='Phase A Percentage Difference'))
    fig_diff.add_trace(go.Scatter(x=test_count, y=phaseB_percentage_difference, name='Phase B Percentage Difference'))

    fig_diff.update_layout(
        title=name_of_plot + " (% Difference)",
        xaxis_title='Ideal Current',
        yaxis_title='Percentage Difference (%)'
    )

    current_time = datetime.now().strftime("%Y%m%d_%H%M%S")

    with open('csv_output/measurement_test_result_{}_{}.csv'.format(current_time, name_of_plot), 'w', newline='') as csvfile:
        csvwriter = csv.writer(csvfile)
        csvwriter.writerow(['Ideal Current', 'Phase A Yoko Current', 'Phase A Breaker Current', 'Phase A Percentage Difference',
                            'Phase B Yoko Current', 'Phase B Breaker Current', 'Phase B Percentage Difference'])

        while True:
            user_input = input()    # Format has to be <Ideal Current>,<Phase A Yoko Current Reading>,<Phase A Breaker Current Reading>,
                                    #                                  <Phase B Yoko Current Reading>,<Phase B Breaker Current Reading>
                                    # eg: 1,0.15,0.153,0.15,0.165
                                    # eg: 2,0.2,0.19,0.2,0.2
                                    # eg: 3,0.501,0.502,0.499,0.532
                                    # eg: 4,1.003,0.976,1.003,0.799
                                    # eg: 5,2.0,2.04,2.0,2.07
                                    
            if user_input == "shutdown":
                break
            elif user_input == "showplot":
                fig_current.show()
                fig_diff.show()
                continue

            try:
                count, phaseA_yoko_curr, phaseA_breaker_curr, phaseB_yoko_curr, phaseB_breaker_curr = map(float, user_input.split(','))
            except ValueError:
                print("Invalid input. Please try again.")
                continue

            test_count.append(count)
            phaseA_yoko_current.append(phaseA_yoko_curr)
            phaseA_breaker_current.append(phaseA_breaker_curr)
            phaseB_yoko_current.append(phaseB_yoko_curr)
            phaseB_breaker_current.append(phaseB_breaker_curr)

            # Calculate percentage difference for Phase A
            try:
                phaseA_diff = (phaseA_breaker_curr - phaseA_yoko_curr) / phaseA_yoko_curr * 100.0
            except ZeroDivisionError:
                phaseA_diff = 1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
            phaseA_percentage_difference.append(phaseA_diff)

            # Calculate percentage difference for Phase B
            try:
                phaseB_diff = (phaseB_breaker_curr - phaseB_yoko_curr) / phaseB_yoko_curr * 100.0
            except ZeroDivisionError:
                phaseB_diff = 1000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000
            phaseB_percentage_difference.append(phaseB_diff)

            fig_current.data[0].x = test_count
            fig_current.data[0].y = phaseA_yoko_current
            fig_current.data[1].x = test_count
            fig_current.data[1].y = phaseA_breaker_current
            fig_current.data[2].x = test_count
            fig_current.data[2].y = phaseB_yoko_current
            fig_current.data[3].x = test_count
            fig_current.data[3].y = phaseB_breaker_current

            fig_diff.data[0].x = test_count
            fig_diff.data[0].y = phaseA_percentage_difference
            fig_diff.data[1].x = test_count
            fig_diff.data[1].y = phaseB_percentage_difference

            csvwriter.writerow([count, phaseA_yoko_curr, phaseA_breaker_curr, phaseA_diff,
                                phaseB_yoko_curr, phaseB_breaker_curr, phaseB_diff])

if __name__ == '__main__':
    name_of_plot = str(sys.argv[1])
    
    plot_real_time_data(name_of_plot)
