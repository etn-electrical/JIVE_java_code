import sys
from datetime import datetime
import csv
import plotly.graph_objects as go

def plot_real_time_data(name_of_plot):
    reading_count = []
    phaseA_yoko_reading = []
    phaseA_breaker_reading = []
    phaseB_yoko_reading = []
    phaseB_breaker_reading = []
    phaseA_percentage_difference = []
    phaseB_percentage_difference = []

    fig_reading = go.Figure()
    fig_diff = go.Figure()

    fig_reading.add_trace(go.Scatter(x=reading_count, y=phaseA_yoko_reading, name='Phase A Yoko Reading'))
    fig_reading.add_trace(go.Scatter(x=reading_count, y=phaseA_breaker_reading, name='Phase A Breaker Reading'))
    fig_reading.add_trace(go.Scatter(x=reading_count, y=phaseB_yoko_reading, name='Phase B Yoko Reading'))
    fig_reading.add_trace(go.Scatter(x=reading_count, y=phaseB_breaker_reading, name='Phase B Breaker Reading'))

    fig_reading.update_layout(
        title=name_of_plot,
        xaxis_title='Reading Count',
        yaxis_title='Readings'
    )

    fig_diff.add_trace(go.Scatter(x=reading_count, y=phaseA_percentage_difference, name='Phase A Percentage Difference'))
    fig_diff.add_trace(go.Scatter(x=reading_count, y=phaseB_percentage_difference, name='Phase B Percentage Difference'))

    fig_diff.update_layout(
        title=name_of_plot + " (%)",
        xaxis_title='Reading Count',
        yaxis_title='Percentage Difference (%)'
    )

    current_time = datetime.now().strftime("%Y%m%d_%H%M%S")

    csv_file = open('csv_output/{}_result_{}.csv'.format(name_of_plot, current_time), 'w', newline='')
    csv_writer = csv.writer(csv_file)
    csv_writer.writerow(['Reading Count', 'Phase A Yoko Reading', 'Phase A Breaker Reading',
                         'Phase B Yoko Reading', 'Phase B Breaker Reading',
                         'Phase A Percentage Difference', 'Phase B Percentage Difference'])

    while True:
        user_input = input()    # Format has to be <Reading count>,<Phase A Yoko Reading>,<Phase A Breaker Reading>,
                                #                               <Phase B Yoko Reading>,<Phase B Breaker Reading>
                                # eg: 1,10,20.1,15,19.5

        if user_input == "shutdown":
            csv_file.close()
            break
        elif user_input == "showplot":
            fig_reading.show()
            fig_diff.show()
            continue

        try:
            count, phaseA_yoko_read, phaseA_breaker_read, phaseB_yoko_read, phaseB_breaker_read = map(float, user_input.split(','))
        except ValueError:
            print("Invalid input. Please try again.")
            continue

        reading_count.append(count)
        phaseA_yoko_reading.append(phaseA_yoko_read)
        phaseA_breaker_reading.append(phaseA_breaker_read)
        phaseB_yoko_reading.append(phaseB_yoko_read)
        phaseB_breaker_reading.append(phaseB_breaker_read)

        # Calculate percentage difference for Phase A
        phaseA_diff = (phaseA_breaker_read - phaseA_yoko_read) / phaseA_yoko_read * 100.0
        phaseA_percentage_difference.append(phaseA_diff)

        # Calculate percentage difference for Phase B
        phaseB_diff = (phaseB_breaker_read - phaseB_yoko_read) / phaseB_yoko_read * 100.0
        phaseB_percentage_difference.append(phaseB_diff)

        fig_reading.data[0].x = reading_count
        fig_reading.data[0].y = phaseA_yoko_reading
        fig_reading.data[1].x = reading_count
        fig_reading.data[1].y = phaseA_breaker_reading
        fig_reading.data[2].x = reading_count
        fig_reading.data[2].y = phaseB_yoko_reading
        fig_reading.data[3].x = reading_count
        fig_reading.data[3].y = phaseB_breaker_reading

        fig_diff.data[0].x = reading_count
        fig_diff.data[0].y = phaseA_percentage_difference
        fig_diff.data[1].x = reading_count
        fig_diff.data[1].y = phaseB_percentage_difference

        csv_writer.writerow([count, phaseA_yoko_read, phaseA_breaker_read,
                             phaseB_yoko_read, phaseB_breaker_read,
                             phaseA_diff, phaseB_diff])

if __name__ == '__main__':
    name_of_plot = str(sys.argv[1])
    
    plot_real_time_data(name_of_plot)
